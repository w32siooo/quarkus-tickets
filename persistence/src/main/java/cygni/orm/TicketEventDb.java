package cygni.orm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@Builder
@ToString
@NamedQuery(name="Tickets.findAllByEventId", query="SELECT f FROM TicketEventDb f WHERE f.eventId = :eventId ORDER BY f.createdAt")
@NamedQuery(name = "Tickets.findAllByEventIdAndUserId", query = "SELECT f FROM TicketEventDb f WHERE f.eventId = :eventId AND f.userId = :userId ORDER BY f.createdAt")
public class TicketEventDb  {

    @Id
    @SequenceGenerator(name = "ticketSeq", sequenceName = "ticket_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "ticketSeq")
    private Long id;

    @Column(length = 1000)
    private String data;

    private UUID userId;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @CreationTimestamp
    private ZonedDateTime createdAt;

}
