package cygni.users.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserAggregate extends AggregateRoot {
    public static final String AGGREGATE_TYPE = "User";

    private String name;

    private long balance;

    private List<UUID> ownedExperiences;

    public UserAggregate(UUID id) {
        super(id, AGGREGATE_TYPE);
        ownedExperiences = new ArrayList<>();
    }

    public void buyExperience(UUID experienceId ,int seats, Long price) {
        if (balance < price) {
            throw new RuntimeException("Not enough balance to book experience ticket");
        }
        final var data = new BuyExperienceEvent(id, experienceId, seats, price);
        final var dataB = SerializerUtils.serializeToJsonBytes(data);
        final var ev = this.createEvent(BuyExperienceEvent.BUY_EXPERIENCE_EVENT, dataB, null);
        this.apply(ev);
    }

    public void createNewUser(String name, long balance) {
        final var data = new UserCreatedEvent(id ,name, balance);
        final var dataB = SerializerUtils.serializeToJsonBytes(data);
        final var ev = this.createEvent(UserCreatedEvent.USER_CREATED, dataB, null);
        this.apply(ev);
    }

    public void depositBalance(long amount) {
        final var data = new UserBalanceAddedEvent(id, amount);
        final var dataB = SerializerUtils.serializeToJsonBytes(data);
        final var ev = this.createEvent(UserBalanceAddedEvent.USER_BALANCE_CHANGED, dataB, null);
        this.apply(ev);
    }

    public void removeExperience(UUID experienceId) {
        final var data = new UserExperienceRemovedEvent(id, experienceId);
        final var dataB = SerializerUtils.serializeToJsonBytes(data);
        final var ev = this.createEvent(UserExperienceRemovedEvent.USER_EXPERIENCE_REMOVED, dataB, null);
        this.apply(ev);
    }

    @Override
    public void when(Event event) {
        switch (event.getType()) {
            case UserCreatedEvent.USER_CREATED -> {
                UserCreatedEvent userCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserCreatedEvent.class);
                this.name = userCreatedEvent.getName();
                this.balance = userCreatedEvent.getBalance();
            }
            case UserBalanceAddedEvent.USER_BALANCE_CHANGED -> {
                UserBalanceAddedEvent userBalanceAddedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserBalanceAddedEvent.class);
                this.balance = this.balance + userBalanceAddedEvent.getToAdd();
            }
            case BuyExperienceEvent.BUY_EXPERIENCE_EVENT -> {
                BuyExperienceEvent buyExperienceEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BuyExperienceEvent.class);
                this.ownedExperiences.add(buyExperienceEvent.getExperienceId());
            }
            case UserExperienceRemovedEvent.USER_EXPERIENCE_REMOVED -> {
                UserExperienceRemovedEvent userExperienceRemovedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserExperienceRemovedEvent.class);
                this.ownedExperiences.remove(userExperienceRemovedEvent.getExperienceId());
            }
        }

    }


}
