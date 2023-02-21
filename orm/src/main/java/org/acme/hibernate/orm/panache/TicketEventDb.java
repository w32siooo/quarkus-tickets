package org.acme.hibernate.orm.panache;

import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@Builder
public class TicketEventDb extends PanacheEntity {

    @Column(length = 100)
    private String data;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @CreationTimestamp
    private ZonedDateTime createdAt;

}
