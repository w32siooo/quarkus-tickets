package cygni.experiences.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.experiences.events.ExperienceCreatedEvent;
import cygni.experiences.events.ExperienceSeatsChangedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ExperienceAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "Experience";

    private String artist;
    private String venue;
    private String date;
    private int price;
    private int seats;

    public ExperienceAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    public void createExperience(String artist, String venue, String date, int price, int seats) {
        final var data = new ExperienceCreatedEvent(id, artist, venue, date, price, seats);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceCreatedEvent.EXPERIENCE_CREATED, dataB, null);
        log.info("Created event: {}", event.getId());
        this.apply(event);
    }

    public void changeSeats(int newSeats){
        final var data = new ExperienceSeatsChangedEvent(id,newSeats);
        final byte[] dataB = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(ExperienceSeatsChangedEvent.EXPERIENCE_SEATS_CHANGED_EVENTS,dataB,null);
        this.apply(event);
    }


    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case ExperienceSeatsChangedEvent.EXPERIENCE_SEATS_CHANGED_EVENTS ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceSeatsChangedEvent.class));
            case ExperienceCreatedEvent.EXPERIENCE_CREATED ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), ExperienceCreatedEvent.class));

            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        }
    }

    private void handle(ExperienceCreatedEvent event) {
        this.artist = event.getArtist();
        this.venue = event.getVenue();
        this.date = event.getDate();
        this.price = event.getPrice();
        this.seats = event.getSeats();
    }

    private void handle(ExperienceSeatsChangedEvent event) {
        this.seats = event.getNewSeats();
    }
}
