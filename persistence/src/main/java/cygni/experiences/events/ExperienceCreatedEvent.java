package cygni.experiences.events;

import cygni.es.BaseEvent;
import cygni.experiences.aggregates.ExperienceAggregate;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ExperienceCreatedEvent extends BaseEvent {
  public static final String EXPERIENCE_CREATED = "ExperienceCreated";
  public static final String AGGREGATE_TYPE = ExperienceAggregate.AGGREGATE_TYPE;

  private final String artist;
  private final String venue;
  private final String date;
  private final int price;
  private final int seats;

  public ExperienceCreatedEvent(
      UUID aggregateId, String artist, String venue, String date, int price, int seats) {
    super(aggregateId);
    this.artist = artist;
    this.venue = venue;
    this.date = date;
    this.price = price;
    this.seats = seats;
  }
}
