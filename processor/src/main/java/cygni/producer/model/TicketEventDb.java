package cygni.producer.model;

import cygni.producer.panache.EventType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.time.ZonedDateTime;


@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@Builder
public class TicketEventDb   {

    private String data;

    private String eventId;

    private EventType eventType;

    private ZonedDateTime createdAt;

}
