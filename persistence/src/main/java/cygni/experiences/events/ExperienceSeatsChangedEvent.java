package cygni.experiences.events;

import cygni.es.BaseEvent;
import cygni.experiences.aggregates.ExperienceAggregate;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ExperienceSeatsChangedEvent extends BaseEvent {

    public static final String EXPERIENCE_SEATS_CHANGED_EVENTS = "ExperienceSeatsChangedEvents";

    public static final String AGGREGATE_TYPE = ExperienceAggregate.AGGREGATE_TYPE;

    private final int newSeats;

    public ExperienceSeatsChangedEvent(UUID aggregateId, int newSeats){
        super(aggregateId);
        this.newSeats = newSeats;
    }

}
