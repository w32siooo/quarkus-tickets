package cygni.experiences.aggregates;

import com.fasterxml.jackson.annotation.JsonAlias;
import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.es.exceptions.BookingFailedException;
import cygni.experiences.dtos.ExperienceAggregateViewDTO;
import cygni.experiences.events.ExperienceBookedEvent;
import cygni.experiences.events.ExperienceCancelledEvent;
import cygni.experiences.events.ExperienceCreatedEvent;
import cygni.experiences.events.ExperienceTotalSeatsChangedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperienceAggregate extends AggregateRoot {

  public static final String AGGREGATE_TYPE = "Experience";

  private static final Logger log = LoggerFactory.getLogger(ExperienceAggregate.class);

  private String artist;
  private String venue;
  private String date;
  private int price;
  private int totalSeats;

  private int availableSeats;

  @JsonAlias({"bookedSeats", "soldSeats"})
  private int soldSeats;

  private boolean cancelled = false;

  private List<String> notes;

  public ExperienceAggregate(UUID id) {
    super(id, AGGREGATE_TYPE);
    notes = new ArrayList<>();
  }

  public ExperienceAggregate(String artist, String venue, String date, int price, int totalSeats, int availableSeats, int soldSeats, boolean cancelled, List<String> notes) {
    this.artist = artist;
    this.venue = venue;
    this.date = date;
    this.price = price;
    this.totalSeats = totalSeats;
    this.availableSeats = availableSeats;
    this.soldSeats = soldSeats;
    this.cancelled = cancelled;
    this.notes = notes;
  }

  public void createExperience(String artist, String venue, String date, int price, int seats) {
    final var data = new ExperienceCreatedEvent(id, artist, venue, date, price, seats);
    final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
    final var event = this.createEvent(ExperienceCreatedEvent.EXPERIENCE_CREATED, dataB, null);
    log.info("Created event: {}", event.getId());
    this.apply(event);
  }

  public void changeTotalSeats(int newSeats) {
    final var data = new ExperienceTotalSeatsChangedEvent(id, newSeats);
    final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
    final var event =
        this.createEvent(
            ExperienceTotalSeatsChangedEvent.EXPERIENCE_TOTAL_SEATS_CHANGED, dataB, null);
    this.apply(event);
  }

  public void cancelExperience(String reason) {
    final var data = new ExperienceCancelledEvent(id, reason);
    final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
    final var event = this.createEvent(ExperienceCancelledEvent.EXPERIENCE_CANCELLED, dataB, null);
    this.apply(event);
  }

  public void bookExperience(UUID userId, int seats) {
    if (seats > availableSeats) throw new BookingFailedException("not enough seats available");
    if (cancelled) throw new BookingFailedException("experience is cancelled");

    final var data = new ExperienceBookedEvent(id, userId, seats);
    final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
    final var event = this.createEvent(ExperienceBookedEvent.EXPERIENCE_BOOKED, dataB, null);
    this.apply(event);
  }

  @Override
  public void when(Event event) {
    switch (event.getType()) {
      case ExperienceTotalSeatsChangedEvent.EXPERIENCE_TOTAL_SEATS_CHANGED -> handle(
          SerializerUtils.deserializeFromJsonBytes(
              event.getData(), ExperienceTotalSeatsChangedEvent.class));
      case ExperienceCreatedEvent.EXPERIENCE_CREATED -> handle(
          SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceCreatedEvent.class));

      case ExperienceBookedEvent.EXPERIENCE_BOOKED -> {
        final var experienceBookedEvent =
            SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceBookedEvent.class);
        this.soldSeats += experienceBookedEvent.getSeats();
        this.availableSeats = this.totalSeats - this.soldSeats;
        notes.add("Booked by " + experienceBookedEvent.getUserId());

      }
      case ExperienceCancelledEvent.EXPERIENCE_CANCELLED -> {
        this.cancelled = true;
        notes.add(
            "Cancellation reason: "
                + SerializerUtils.deserializeFromJsonBytes(
                        event.getData(), ExperienceCancelledEvent.class)
                    .getReason());
      }
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getType());
    }
  }

  public ExperienceAggregateViewDTO toDTO() {
    return new ExperienceAggregateViewDTO(
        id,
        artist,
        venue,
        date,
        price,
        totalSeats,
        availableSeats,
        soldSeats,
        cancelled,
        notes.toArray(new String[0]));
  }

  private void handle(ExperienceCreatedEvent event) {
    this.artist = event.getArtist();
    this.venue = event.getVenue();
    this.date = event.getDate();
    this.price = event.getPrice();
    this.totalSeats = event.getSeats();
    this.availableSeats = event.getSeats();
    this.soldSeats = 0;
  }

  private void handle(ExperienceTotalSeatsChangedEvent event) {
    this.totalSeats = event.getNewSeats();
    this.availableSeats = this.totalSeats - this.soldSeats;
  }

  public String getArtist() {
    return artist;
  }

  public String getVenue() {
    return venue;
  }

  public String getDate() {
    return date;
  }

  public int getPrice() {
    return price;
  }

  public int getTotalSeats() {
    return totalSeats;
  }

  public int getAvailableSeats() {
    return availableSeats;
  }

  public int getSoldSeats() {
    return soldSeats;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public List<String> getNotes() {
    return notes;
  }
}
