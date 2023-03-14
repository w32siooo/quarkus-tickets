package cygni.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.dtos.OrderFulfilledDto;
import cygni.dtos.ResponseDto;
import cygni.events.TicketActivateEvent;
import cygni.aggregates.TicketAggregate;
import cygni.events.TicketCreateEvent;
import cygni.events.TicketOrderEvent;
import cygni.orm.EventData;
import cygni.orm.EventType;
import cygni.orm.TicketEventDb;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Slf4j
public class TicketService {
    @Inject
    Mutiny.SessionFactory sf;

    @Inject
    ObjectMapper objectMapper;

    public Uni<ResponseDto> orderTicket(TicketOrderEvent ev) {
        return getTicketsForUser(ev.getEventId(), ev.getUserId())
                .map(Unchecked.function(agg ->
                {
                    var event = TicketEventDb.builder().eventId(ev.getEventId()).data(objectMapper.writeValueAsString
                                    (
                                            new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType())
                                    )
                            )
                            .eventType(EventType.TICKET_ORDERED).userId(ev.getUserId()).build();
                    agg.applyEvent(objectMapper.readValue(event.getData(), EventData.class));
                    return event;
                }))
                .flatMap(okEvent ->
                        sf.withTransaction(session -> session.persist(okEvent))
                                .replaceWith(OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build()));

    }

    public Uni<ResponseDto> activateTicket(TicketActivateEvent ev) {
        return getTicketsForUser(ev.getEventId(), ev.getUserId())
                .map(Unchecked.function(agg ->
                {
                    var event = TicketEventDb.builder().eventId(ev.getEventId()).data(objectMapper.writeValueAsString
                                    (
                                            new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType())
                                    )
                            )
                            .eventType(EventType.TICKET_ACTIVATED).userId(ev.getUserId()).build();
                    agg.applyEvent(objectMapper.readValue(event.getData(), EventData.class));
                    return event;
                }))
                .flatMap(okEvent ->
                        sf.withTransaction(session -> session.persist(okEvent))
                                .replaceWith(OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build()));
    }

    public Uni<ResponseDto> createTicket(TicketCreateEvent ev) {
        return Uni.createFrom().item(ev).map(Unchecked.function(event -> {
                            TicketEventDb ticketEventDb = TicketEventDb.builder().eventId(event.getEventId()).data(objectMapper.writeValueAsString
                                            (
                                                    new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType())
                                            )
                                    )
                                    .eventType(EventType.TICKET_CREATED).userId(event.getUserId()).build();
                            log.info(ticketEventDb.toString());
                            return ticketEventDb;
                        }
                ))
                .flatMap(ticketEventDb -> sf.withTransaction(session -> session.persist(ticketEventDb))
                        .replaceWith(OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build()));
    }


    public Uni<TicketAggregate> getTicketsByEventId(String eventId) {
        TicketAggregate ticketAggregate = new TicketAggregate(eventId);
        return this.sf.withStatelessSession(
                (session) ->
                        session
                                .createNamedQuery("Tickets.findAllByEventId", TicketEventDb.class)
                                .setParameter("eventId", eventId)
                                .getResultList()
                                .onItem().ifNotNull().transformToMulti(resultList ->
                                        Multi.createFrom().iterable(resultList)
                                                .map(Unchecked.function(
                                                        event ->
                                                                objectMapper.readValue(event.getData(), EventData.class)

                                                )).onItem().invoke(ticketAggregate::applyEvent)
                                                .select().last()).toUni().replaceWith(ticketAggregate)
        );
    }

    public Uni<TicketAggregate> getTicketsForUser(String eventId, UUID userId) {
        TicketAggregate ticketAggregate = new TicketAggregate(eventId);

        return this.sf.withStatelessSession(
                (session) ->
                        session
                                .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                                .setParameter("eventId", eventId)
                                .setParameter("userId", userId)
                                .getResultList()
                                .onItem().ifNotNull().transformToMulti(resultList ->
                                        Multi.createFrom().iterable(resultList)
                                                .map(Unchecked.function(
                                                        event ->
                                                                objectMapper.readValue(event.getData(), EventData.class)

                                                )).onItem().invoke(ticketAggregate::applyEvent)
                                                .select().last()).toUni().replaceWith(ticketAggregate)
        );
    }


}
