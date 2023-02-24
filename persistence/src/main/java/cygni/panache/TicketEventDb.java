package cygni.panache;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@Builder
public class TicketEventDb  {

    @Id
    @SequenceGenerator(name = "ticketSeq", sequenceName = "ticket_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "ticketSeq")
    Long id;

    @Column(length = 100)
    private String data;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @CreationTimestamp
    private ZonedDateTime createdAt;

}
