package cygni.users.components;

import cygni.core.eventsourcing.EventStore;
import cygni.core.eventsourcing.dto.RequestAcceptedDTO;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.users.aggregates.UserAggregate;
import cygni.users.dtos.BuyTicketDTO;
import cygni.users.dtos.CreateNewUserDTO;
import cygni.users.dtos.RemoveTicketDTO;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UserCommandHandler implements UserCommandService {

  @Inject EventStore eventStore;

  @Override
  public Uni<RequestAcceptedDTO> handle(CreateNewUserDTO createUserDTO) {
    UUID id = UUID.randomUUID();
    UserAggregate userAggregate = new UserAggregate(id);
    userAggregate.createNewUser(createUserDTO.name(), createUserDTO.balance());
    return eventStore
        .persistAndPublish(userAggregate)
        .map(ignored -> new RequestAcceptedDTO("user created", id));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateId, BuyTicketDTO buyTicketDTO) {

    return eventStore
        .load(buyTicketDTO.experienceId(), ExperienceAggregate.class)
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
                                buyTicketDTO.experienceId(),
                                buyTicketDTO.seats(),
                                (long) expAgg.getPrice());
                            return aggregate;
                          });
              return Uni.combine().all().unis(Uni.createFrom().item(expAgg), userAgg).asTuple();
            })
        .onItem()
        .transform(
            tuple -> {
              ExperienceAggregate expAgg = tuple.getItem1();
              UserAggregate userAgg = tuple.getItem2();
              expAgg.bookExperience(aggregateId, buyTicketDTO.seats());
              return Uni.combine()
                  .all()
                  .unis(eventStore.persistAndPublish(expAgg), eventStore.persistAndPublish(userAgg))
                  .asTuple();
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
