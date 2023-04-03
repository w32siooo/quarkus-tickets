package cygni.users.aggregates;

import cygni.core.eventsourcing.AggregateRoot;
import cygni.core.eventsourcing.Event;
import cygni.core.eventsourcing.SerializerUtils;
import cygni.users.dtos.UserViewDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

  public void buyExperience(UUID experienceId, int seats, Long price) {
    if (balance < price) {
      throw new RuntimeException("Not enough balance to book experience ticket");
    }
    final var data = new BuyExperienceEvent(id, experienceId, seats, price);
    final var dataB = SerializerUtils.serializeToJsonBytes(data);
    final var ev = this.createEvent(BuyExperienceEvent.BUY_EXPERIENCE_EVENT, dataB, null);
    this.apply(ev);
  }

  public void createNewUser(String name, long balance) {
    final var data = new UserCreatedEvent(id, name, balance);
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
    final var ev =
        this.createEvent(UserExperienceRemovedEvent.USER_EXPERIENCE_REMOVED, dataB, null);
    this.apply(ev);
  }

  @Override
  public void when(Event event) {
    switch (event.getType()) {
      case UserCreatedEvent.USER_CREATED -> handle(
          SerializerUtils.deserializeFromJsonBytes(event.getData(), UserCreatedEvent.class));
      case UserBalanceAddedEvent.USER_BALANCE_CHANGED -> handle(
          SerializerUtils.deserializeFromJsonBytes(event.getData(), UserBalanceAddedEvent.class));
      case BuyExperienceEvent.BUY_EXPERIENCE_EVENT -> handle(
          SerializerUtils.deserializeFromJsonBytes(event.getData(), BuyExperienceEvent.class));
      case UserExperienceRemovedEvent.USER_EXPERIENCE_REMOVED -> handle(
          SerializerUtils.deserializeFromJsonBytes(
              event.getData(), UserExperienceRemovedEvent.class));
    }
  }

  private void handle(UserCreatedEvent event) {
    this.name = event.getName();
    this.balance = event.getBalance();
  }

  private void handle(UserBalanceAddedEvent event) {
    this.balance = this.balance + event.getToAdd();
  }

  private void handle(BuyExperienceEvent event) {
    this.ownedExperiences.add(event.getExperienceId());
  }

  private void handle(UserExperienceRemovedEvent event) {
    this.ownedExperiences.remove(event.getExperienceId());
  }

  public UserViewDTO toDTO() {
    return new UserViewDTO(name, balance, id, ownedExperiences);
  }
}
