package cygni.users.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.users.dtos.UserViewDTO;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAggregate extends AggregateRoot {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(UserAggregate.class);
  public static final String AGGREGATE_TYPE = "User";

  private String name;

  private long balance;

  private final List<UUID> ownedExperiences;

  public UserAggregate(UUID id) {
    super(id, AGGREGATE_TYPE);
    ownedExperiences = new ArrayList<>();
  }

  public UserAggregate(String name, long balance, List<UUID> ownedExperiences) {
    this.name = name;
    this.balance = balance;
    this.ownedExperiences = ownedExperiences;
  }


  public void buyExperience(UUID experienceId, int seats, Long price) {
    if (balance < price * seats) {
      throw new RuntimeException("Not enough balance to book the wanted tickets");
    }else{
      log.info("Buying with balance {} for {} seats", balance, seats);
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
    if (event.getPrice() != null) {
      this.balance = this.balance - event.getPrice() * (long) event.getSeats();
    }
    this.ownedExperiences.add(event.getExperienceId());
  }

  private void handle(UserExperienceRemovedEvent event) {
    this.ownedExperiences.remove(event.getExperienceId());
  }

  public UserViewDTO toDTO() {
    return new UserViewDTO(name, balance, id, ownedExperiences);
  }

  public String getName() {
    return name;
  }

  public long getBalance() {
    return balance;
  }

  public List<UUID> getOwnedExperiences() {
    return ownedExperiences;
  }
}
