package cygni.users.components;

import cygni.es.EventStore;
import cygni.es.dto.RequestAcceptedDTO;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.users.aggregates.UserAggregate;
import cygni.users.commands.CreateNewUserCommand;
import cygni.users.dtos.BuyTicketDTO;
import cygni.users.dtos.RemoveTicketDTO;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserCommandHandler implements UserCommandService {

  @Inject EventStore eventStore;

  @Override
  public Uni<RequestAcceptedDTO> handle(CreateNewUserCommand createUserDTO) {
    UserAggregate userAggregate = new UserAggregate(createUserDTO.id());
    userAggregate.createNewUser(createUserDTO.name(), createUserDTO.balance());
    return eventStore
        .persistAndPublish(userAggregate)
        .map(ignored -> new RequestAcceptedDTO("user created", createUserDTO.id()));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateId, BuyTicketDTO buyTicketDTO) {

    return eventStore
        .load(buyTicketDTO.experienceId(), ExperienceAggregate.class) //first load the experience to get the id
        .onItem()
        .transformToUni(
            expAgg -> {
              var userAgg =
                  eventStore
                      .load(aggregateId, UserAggregate.class)
                      .onItem()
                      .transform(
                          aggregate -> {
                            aggregate.buyExperience(
                                expAgg.getId(),
                                buyTicketDTO.seats(),
                                (long) expAgg.getPrice());
                              expAgg.bookExperience(aggregateId, buyTicketDTO.seats());

                              return aggregate;
                          });
              return Uni.combine().all().unis(Uni.createFrom().item(expAgg), userAgg).asTuple();
            })
        .onItem()
        .transformToUni(
            tuple -> {
              ExperienceAggregate expAgg = tuple.getItem1();
              UserAggregate userAgg = tuple.getItem2();
              return Uni.join()
                  .all(eventStore.persistAndPublish(expAgg), eventStore.persistAndPublish(userAgg))
                  .andCollectFailures();
            })
        .onItem()
        .transform(tuple -> new RequestAcceptedDTO("ticket bought", aggregateId));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateId, RemoveTicketDTO removeTicketDTO) {

    return eventStore
        .load(aggregateId, UserAggregate.class)
        .onItem()
        .transformToUni(
            aggregate -> {
              aggregate.removeExperience(removeTicketDTO.experienceId());
              return eventStore
                  .persistAndPublish(aggregate)
                  .map(ignored -> new RequestAcceptedDTO("ticket removed", aggregateId));
            });
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID userId, Long newBalance) {

    return eventStore
        .load(userId, UserAggregate.class)
        .onItem()
        .transformToUni(
            aggregate -> {
              aggregate.depositBalance(newBalance);
              return eventStore
                  .persistAndPublish(aggregate)
                  .map(ignored -> new RequestAcceptedDTO("balance updated", userId));
            });
  }
}
