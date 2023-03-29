package cygni.experiences.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.experiences.events.ExperienceBookedEvent;
import cygni.experiences.events.ExperienceCancelledEvent;
import cygni.experiences.events.ExperienceCreatedEvent;
import cygni.experiences.events.ExperienceTotalSeatsChangedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ExperienceAggregate extends AggregateRoot  {

    public static final String AGGREGATE_TYPE = "Experience";

    private String artist;
    private String venue;
    private String date;
    private int price;
    private int totalSeats;

    private int availableSeats;

    private int soldSeats;

    private boolean cancelled = false;

    private List<String> notes;

    public ExperienceAggregate(UUID id) {
        super(id, AGGREGATE_TYPE);
        notes = new ArrayList<>();
    }

    public void createExperience(String artist, String venue, String date, int price, int seats) {
        final var data = new ExperienceCreatedEvent(id, artist, venue, date, price, seats);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceCreatedEvent.EXPERIENCE_CREATED, dataB, null);
        log.info("Created event: {}", event.getId());
        this.apply(event);
    }

    public void changeTotalSeats(int newSeats){
        final var data = new ExperienceTotalSeatsChangedEvent(id,newSeats);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceTotalSeatsChangedEvent.EXPERIENCE_TOTAL_SEATS_CHANGED,dataB,null);
        this.apply(event);
    }

    public void cancelExperience(String reason) {
        final var data = new ExperienceCancelledEvent(id, reason);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceCancelledEvent.EXPERIENCE_CANCELLED, dataB, null);
        this.apply(event);
    }

    public void bookExperience(UUID userId, int seats){
        if (seats > availableSeats) throw new IllegalArgumentException("Not enough seats available");
        if (cancelled) throw new IllegalArgumentException("Experience is cancelled");

        final var data = new ExperienceBookedEvent(id, userId, seats);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceBookedEvent.EXPERIENCE_BOOKED, dataB, null);
        this.apply(event);
    }


    @Override
    public void when(Event event) {
        switch (event.getType()) {
            case ExperienceTotalSeatsChangedEvent.EXPERIENCE_TOTAL_SEATS_CHANGED ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceTotalSeatsChangedEvent.class));
            case ExperienceCreatedEvent.EXPERIENCE_CREATED ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceCreatedEvent.class));
            case ExperienceBookedEvent.EXPERIENCE_BOOKED -> {
                final var experienceBookedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceBookedEvent.class);
                this.soldSeats += experienceBookedEvent.getSeats();
                this.availableSeats = this.totalSeats - this.soldSeats;
                notes.add("Booked by " + experienceBookedEvent.getUserId());
            }
            case ExperienceCancelledEvent.EXPERIENCE_CANCELLED -> {
                this.cancelled = true;
                notes.add("Cancellation reason: " +SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceCancelledEvent.class).getReason());
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        }
    }

    private void handle(ExperienceCreatedEvent event) {
        this.artist = event.getArtist();
        this.venue = event.getVenue();
        this.date = event.getDate();
        this.price = event.getPrice();
        this.totalSeats = event.getSeats();
    }

    private void handle(ExperienceTotalSeatsChangedEvent event) {
        this.totalSeats = event.getNewSeats();
    }
}
