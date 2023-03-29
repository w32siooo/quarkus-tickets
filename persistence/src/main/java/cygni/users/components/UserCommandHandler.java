package cygni.users.components;

import cygni.es.EventStore;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.users.aggregates.UserAggregate;
import cygni.users.dtos.BuyTicketDTO;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UserCommandHandler implements UserCommandService {

    @Inject
    EventStore eventStore;

    @Override
    public Uni<Void> handle(String name, Long balance) {
        UUID id = UUID.randomUUID();
        UserAggregate userAggregate = new UserAggregate(id);
        userAggregate.createNewUser(name, balance);
        return eventStore.persistAndPublish(userAggregate);
    }

    @Override
    public Uni<Void> handle(UUID aggregateId, BuyTicketDTO buyTicketDTO) {

        return eventStore.load(buyTicketDTO.experienceId(), ExperienceAggregate.class)
                .onItem().transformToUni(expAgg -> {
                    var userAgg = eventStore.load(aggregateId, UserAggregate.class).onItem()
                            .transform(aggregate -> {
                                aggregate.buyExperience(buyTicketDTO.experienceId(), buyTicketDTO.seats(), (long) expAgg.getPrice());
                                return aggregate;
                            });

                    return Uni.combine().all().unis(Uni.createFrom().item(expAgg), userAgg).asTuple();
                }).onItem().transform(
                        tuple -> {
                            ExperienceAggregate expAgg = tuple.getItem1();
                            UserAggregate userAgg = tuple.getItem2();
                            expAgg.bookExperience(aggregateId, buyTicketDTO.seats());
                            return Uni.combine().all().unis(eventStore.persistAndPublish(expAgg), eventStore.persistAndPublish(userAgg)).asTuple();
                        }
                ).onItem().transformToUni(tuple -> Uni.createFrom().voidItem()
                );
    }

    @Override
    public Uni<Void> removeTicket(UUID userId, UUID experienceId) {

        return eventStore.load(userId, UserAggregate.class)
                .onItem().transformToUni(aggregate -> {
                    aggregate.removeExperience(experienceId);
                    return eventStore.persistAndPublish(aggregate);
                });
    }

    @Override
    public Uni<Void> handle(UUID userId, Long newBalance) {

        return eventStore.load(userId, UserAggregate.class)
                .onItem().transformToUni(aggregate -> {
                    aggregate.depositBalance(newBalance);
                    return eventStore.persistAndPublish(aggregate);
                });
    }
}
