package cygni.services;

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

@ApplicationScoped
@Slf4j
public class TicketService {
    @Inject
    Mutiny.SessionFactory sf;

    @Inject
    ObjectMapper objectMapper;

    public Uni<ResponseDto> orderTicket(TicketOrderEvent ev) {
        return getTicketsByEventId(ev.getEventId())
                .onItem().ifNotNull().transformToUni(Unchecked.function(ticketAggregate -> {
                    EventData eventData = new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), EventType.TICKET_ORDERED);
                    return ticketAggregate.applyEvent(eventData);
                })).map(Unchecked.function(na -> TicketEventDb.builder().eventId(ev.getEventId()).data(objectMapper.writeValueAsString
                                (
                                        new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType())
                                )
                        )
                        .eventType(EventType.TICKET_ORDERED).userId(ev.getUserId()).build()))
                .flatMap(okEvent ->
                        sf.withTransaction(session -> session.persist(okEvent))
                                .replaceWith(OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build()));
    }

    public Uni<ResponseDto> activateTicket(TicketActivateEvent ev) {
        return getTicketsForUser(ev.getEventId(), ev.getUserId())
                .onItem().ifNotNull().transformToUni(Unchecked.function(ticketAggregate -> {
                    EventData eventData = new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), EventType.TICKET_ACTIVATED);
                    return ticketAggregate.applyEvent(eventData);
                })).map(Unchecked.function(na -> TicketEventDb.builder().eventId(ev.getEventId()).data(objectMapper.writeValueAsString
                                (
                                        new EventData(ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType())
                                )
                        )
                        .eventType(EventType.TICKET_ACTIVATED).userId(ev.getUserId()).build()))
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
        return this.sf.withTransaction(
                (session, transaction) ->
                        session
                                .createNamedQuery("Tickets.findAllByEventId", TicketEventDb.class)
                                .setParameter("eventId", eventId)
                                .getResultList()
                                .flatMap(resultList -> {
                                    TicketAggregate ticketAggregate = new TicketAggregate(eventId);
                                    return Multi.createFrom().iterable(resultList).map(Unchecked.function(
                                            event ->
                                                    objectMapper.readValue(event.getData(), EventData.class)
                                    )).map(ticketAggregate::applyEvent).toUni().replaceWith(ticketAggregate);
                                }));
    }

    public Uni<TicketAggregate> getTicketsForUser(String eventId, UUID userId) {
        TicketAggregate ticketAggregate = new TicketAggregate(eventId);

        return this.sf.withTransaction(
                (session, transaction) ->
                        session
                                .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                                .setParameter("eventId", eventId)
                                .setParameter("userId", userId)
                                .getResultList()
                                .flatMap(resultList ->
                                {
                                    return Multi.createFrom().iterable(resultList)
                                            .map(Unchecked.function(
                                                    event ->
                                                            objectMapper.readValue(event.getData(), EventData.class)

                                            )).map(ticketAggregate::applyEvents).toUni().replaceWith(ticketAggregate);
                                }));
    }


}
