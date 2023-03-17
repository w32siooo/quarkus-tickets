package cygni.tickets.events;

import cygni.es.BaseEvent;
import cygni.tickets.aggregates.ExperienceAggregate;
import lombok.Data;

import java.util.UUID;

@Data
public class ExperienceCreatedEvent extends BaseEvent {
    public static final String EXPERIENCE_CREATED = "ExperienceCreated";
    public static final String AGGREGATE_TYPE = ExperienceAggregate.AGGREGATE_TYPE;

    private String artist;
    private String venue;
    private String date;
    private int price;
    private int seats;

    public ExperienceCreatedEvent(String aggregateId, String artist, String venue, String date, int price, int seats) {
        super(aggregateId);
        this.artist = artist;
        this.venue = venue;
        this.date = date;
        this.price = price;
        this.seats = seats;
    }
}
