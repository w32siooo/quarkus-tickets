package cygni.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.model.OrderFulfilledDto;
import cygni.model.ResponseDto;
import cygni.model.TicketActivatedEvent;
import cygni.model.TicketAggregate;
import cygni.model.TicketCreateEvent;
import cygni.model.TicketOrderEvent;
import cygni.orm.EventData;
import cygni.orm.EventType;
import cygni.orm.TicketEventDb;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Slf4j
public class TicketService {
    @Inject
    Mutiny.SessionFactory sf;

    @Inject
    ObjectMapper objectMapper;


    //order ticket if there are available tickets
    public Uni<ResponseDto> orderTicket(TicketOrderEvent ev) {
        return this.sf.withTransaction(
                        (session, transaction) ->
                                session
                                        .createNamedQuery("Tickets.findAllByEventId", TicketEventDb.class)
                                        .setParameter("eventId", ev.getEventId())
                                        .getResultList()
                                        .map(
                                                Unchecked.function(resultList -> {
                                                    int availableTickets = 0;
                                                    for (TicketEventDb eventDb : resultList) {
                                                        EventData eventData;
                                                        eventData =
                                                                objectMapper.readValue(eventDb.getData(), EventData.class);
                                                        if (eventDb.getEventType().equals(EventType.TICKET_CREATED)) {
                                                            availableTickets += eventData.getQuantity();
                                                        }
                                                        if (eventDb.getEventType().equals(EventType.TICKET_ORDERED)) {
                                                            availableTickets -= eventData.getQuantity();
                                                        }
                                                    }
                                                    if (availableTickets >= ev.getQuantity()) {
                                                        TicketEventDb ticketEventDb = new TicketEventDb();
                                                        ticketEventDb.setEventId(ev.getEventId());
                                                        ticketEventDb.setEventType(EventType.TICKET_ORDERED);
                                                        ticketEventDb.setUserId(UUID.fromString(ev.getUserId()));
                                                        EventData eventData = new EventData(ev.getQuantity(), UUID.fromString(ev.getUserId()));
                                                        ticketEventDb.setData(objectMapper.writeValueAsString(eventData));
                                                        log.info("Ticket ordered for event: " + ev.getEventId());
                                                        log.info(ticketEventDb.toString());
                                                        return ticketEventDb;
                                                    } else {
                                                        log.info("Not enough tickets available for event: {} avaiable tickets: {}", ev.getEventId(), availableTickets);
                                                        throw new RuntimeException("Not enough tickets available");
                                                    }
                                                })
                                        )
                ).onItem().ifNotNull().transformToUni(ticketEventDb -> sf.withTransaction(session -> session.persist(ticketEventDb)))
                .onItem().transform(ticketEventDb -> OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build());
    }

    public Uni<ResponseDto> createTicket(TicketCreateEvent ev) {

        return Uni.createFrom().item(ev).map(Unchecked.function(event ->
                        TicketEventDb.builder().eventId(event.getEventId()).data(objectMapper.writeValueAsString
                                        (new EventData(event.getQuantity(), event.getUserId())))
                                .eventType(EventType.TICKET_CREATED).userId(event.getUserId()).build()))
                .flatMap(ticketEventDb -> sf.withTransaction(session -> session.persist(ticketEventDb))
                        .replaceWith(OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build()));
    }

    public Uni<TicketAggregate> getTicketsForUser(String eventId, UUID userId) {
        AtomicInteger activatedTickets = new AtomicInteger(0);
        AtomicInteger inactiveTickets = new AtomicInteger(0);

        return this.sf.withTransaction(
                (session, transaction) ->
                        session
                                .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                                .setParameter("eventId", eventId)
                                .setParameter("userId", userId)
                                .getResultList()
                                .map(
                                        resultList -> {
                                            resultList.forEach(
                                                    eventDb -> {
                                                        EventData eventData;
                                                        try {
                                                            eventData =
                                                                    objectMapper.readValue(eventDb.getData(), EventData.class);
                                                        } catch (JsonProcessingException e) {
                                                            throw new RuntimeException("Jackson kunne ikke serialisere " + e);
                                                        }
                                                        if (eventDb.getEventType().equals(EventType.TICKET_ORDERED)) {
                                                            inactiveTickets.addAndGet(eventData.getQuantity());

                                                        } else if (eventDb.getEventType().equals(EventType.TICKET_ACTIVATED)) {
                                                            activatedTickets.addAndGet(eventData.getQuantity());
                                                            inactiveTickets.addAndGet(-eventData.getQuantity());
                                                        }
                                                    });

                                            return new TicketAggregate(
                                                    userId,
                                                    activatedTickets.get(),
                                                    inactiveTickets.get(),
                                                    ZonedDateTime.now());
                                        }));
    }

    public Uni<ResponseDto> activateTicket(TicketActivatedEvent ev) {
        return this.sf.withTransaction(
                        (session, transaction) ->
                                session
                                        .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                                        .setParameter("eventId", ev.getEventId())
                                        .setParameter("userId", ev.getUserId())
                                        .getResultList()
                                        .map(
                                                Unchecked.function(
                                                        resultList -> {
                                                            int inactiveTickets = 0;
                                                            for (TicketEventDb eventDb : resultList) {
                                                                EventData eventData;
                                                                eventData =
                                                                        objectMapper.readValue(eventDb.getData(), EventData.class);

                                                                if (eventDb.getEventType().equals(EventType.TICKET_ORDERED)) {
                                                                    inactiveTickets += eventData.getQuantity();
                                                                }
                                                                if (eventDb.getEventType().equals(EventType.TICKET_ACTIVATED)) {
                                                                    inactiveTickets -= eventData.getQuantity();
                                                                }
                                                            }
                                                            if (inactiveTickets >= ev.getQuantity()) {
                                                                TicketEventDb ticketEventDb = new TicketEventDb();
                                                                ticketEventDb.setEventId(ev.getEventId());
                                                                ticketEventDb.setEventType(EventType.TICKET_ACTIVATED);
                                                                ticketEventDb.setUserId(ev.getUserId());
                                                                EventData eventData = new EventData(ev.getQuantity(), ev.getUserId());
                                                                    ticketEventDb.setData(objectMapper.writeValueAsString(eventData));

                                                                log.info("Ticket activated for event: " + ev.getEventId());
                                                                log.info(ticketEventDb.toString());
                                                                return ticketEventDb;
                                                            } else {
                                                                log.info(
                                                                        "Not enough tickets available for event: {} avaiable tickets: {}",
                                                                        ev.getEventId(),
                                                                        inactiveTickets);
                                                                throw new RuntimeException("Not enough tickets available");
                                                            }
                                                        }))
                ).onItem()
                .ifNotNull()
                .transformToUni(ticketEventDb -> sf.withTransaction(s1 -> s1.persist(ticketEventDb)))
                .onItem().transform(ticketEventDb -> OrderFulfilledDto.builder().eventId(ev.getEventId()).quantity(ev.getQuantity()).build());

    }

    ;
}
