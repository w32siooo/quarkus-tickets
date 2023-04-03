package cygni.legacy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.legacy.EventData;
import cygni.legacy.EventType;
import cygni.legacy.TicketEventDb;
import cygni.legacy.aggregates.EventAggregate;
import cygni.legacy.aggregates.UserTicketAggregate;
import cygni.legacy.dtos.OrderFulfilledDto;
import cygni.legacy.dtos.ResponseDto;
import cygni.legacy.events.TicketActivateEvent;
import cygni.legacy.events.TicketCreateEvent;
import cygni.legacy.events.TicketOrderEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
@Slf4j
public class TicketService {
  @Inject Mutiny.SessionFactory sf;

  @Inject ObjectMapper objectMapper;

  public Uni<ResponseDto> orderTicket(TicketOrderEvent ev) {
    return queryByEventId(ev.getEventId())
        .map(
            Unchecked.function(
                agg -> {
                  var eventData =
                      new EventData(
                          ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType());
                  var event =
                      TicketEventDb.builder()
                          .eventId(ev.getEventId())
                          .data(objectMapper.writeValueAsString(eventData))
                          .eventType(EventType.TICKET_ORDERED)
                          .userId(ev.getUserId())
                          .build();
                  return Tuple3.of(eventData, agg, event);
                }))
        .onItem()
        .ifNotNull()
        .invoke(tuple -> tuple.getItem2().applyAndValidateEvent(tuple.getItem1()))
        .onItem()
        .ifNull()
        .failWith(() -> new RuntimeException("No tickets found"))
        .flatMap(
            okEvent ->
                sf.withTransaction(session -> session.persist(okEvent.getItem3()))
                    .replaceWith(
                        OrderFulfilledDto.builder()
                            .eventId(ev.getEventId())
                            .quantity(ev.getQuantity())
                            .build()));
  }

  public Uni<ResponseDto> activateTicket(TicketActivateEvent ev) {
    return queryByUserIdAndEventId(ev.getEventId(), ev.getUserId())
        .map(
            Unchecked.function(
                agg -> {
                  var eventData =
                      new EventData(
                          ev.getUserId(), ev.getEventId(), ev.getQuantity(), ev.getEventType());
                  var event =
                      TicketEventDb.builder()
                          .eventId(ev.getEventId())
                          .data(objectMapper.writeValueAsString(eventData))
                          .eventType(EventType.TICKET_ACTIVATED)
                          .userId(ev.getUserId())
                          .build();
                  return Tuple3.of(eventData, agg, event);
                }))
        .onItem()
        .ifNotNull()
        .invoke(tuple -> tuple.getItem2().applyAndValidateEvent(tuple.getItem1()))
        .onItem()
        .ifNull()
        .failWith(() -> new RuntimeException("No tickets found"))
        .flatMap(
            okEvent ->
                sf.withTransaction(session -> session.persist(okEvent.getItem3()))
                    .replaceWith(
                        OrderFulfilledDto.builder()
                            .eventId(ev.getEventId())
                            .quantity(ev.getQuantity())
                            .build()));
  }

  public Uni<ResponseDto> createTicket(TicketCreateEvent ev) {
    return Uni.createFrom()
        .item(ev)
        .map(
            Unchecked.function(
                event -> {
                  TicketEventDb ticketEventDb =
                      TicketEventDb.builder()
                          .eventId(event.getEventId())
                          .data(
                              objectMapper.writeValueAsString(
                                  new EventData(
                                      ev.getUserId(),
                                      ev.getEventId(),
                                      ev.getQuantity(),
                                      ev.getEventType())))
                          .eventType(EventType.TICKET_CREATED)
                          .userId(event.getUserId())
                          .build();
                  log.info(ticketEventDb.toString());
                  return ticketEventDb;
                }))
        .flatMap(
            ticketEventDb ->
                sf.withTransaction(session -> session.persist(ticketEventDb))
                    .replaceWith(
                        OrderFulfilledDto.builder()
                            .eventId(ev.getEventId())
                            .quantity(ev.getQuantity())
                            .build()));
  }

  public Uni<EventAggregate> queryByEventId(UUID eventId) {
    EventAggregate eventAggregate = new EventAggregate(eventId);
    return this.sf.withSession(
        (session) ->
            session
                .createNamedQuery("Tickets.findAllByEventId", TicketEventDb.class)
                .setParameter("eventId", eventId)
                .getResultList()
                .onItem()
                .ifNotNull()
                .transformToMulti(
                    resultList ->
                        Multi.createFrom()
                            .iterable(resultList)
                            .map(
                                Unchecked.function(
                                    event ->
                                        objectMapper.readValue(event.getData(), EventData.class)))
                            .onItem()
                            .invoke(eventAggregate::applyAndValidateEvent)
                            .select()
                            .last())
                .toUni()
                .replaceWith(eventAggregate));
  }

  public Uni<UserTicketAggregate> queryByUserIdAndEventId(UUID eventId, UUID userId) {
    UserTicketAggregate userTicketAggregate = new UserTicketAggregate(eventId, userId);
    return this.sf.withSession(
        (session) ->
            session
                .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .getResultList()
                .onItem()
                .ifNotNull()
                .transformToMulti(
                    resultList ->
                        Multi.createFrom()
                            .iterable(resultList)
                            .map(
                                Unchecked.function(
                                    event ->
                                        objectMapper.readValue(event.getData(), EventData.class)))
                            .onItem()
                            .invoke(userTicketAggregate::applyAndValidateEvent)
                            .select()
                            .last())
                .toUni()
                .replaceWith(userTicketAggregate));
  }
}
