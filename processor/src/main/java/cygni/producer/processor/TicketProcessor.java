package cygni.producer.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cygni.producer.model.TicketCreateEvent;
import cygni.producer.model.TicketEventDb;
import cygni.producer.panache.EventData;
import cygni.producer.panache.EventType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

/**
 * A bean consuming data from the "quote-requests" RabbitMQ queue and giving out a random quote. The
 * result is pushed to the "quotes" RabbitMQ exchange.
 */
@ApplicationScoped
@Slf4j
public class TicketProcessor {

  private final WebClient webClient;
  private final Vertx vertx;

  @Inject ObjectMapper objectMapper;

  @Inject
  public TicketProcessor(Vertx vertx) {
    this.vertx = vertx;
    this.webClient = WebClient.create(vertx);
  }

  @Incoming("ticket-orders")
  public Uni<Void> consume(JsonObject command) {

      TicketCreateEvent event = command.mapTo(TicketCreateEvent.class);

    log.error(event.toString());

    webClient.post(8085,"127.0.0.1","/hello")
            .sendJson(event)
            .onFailure(throwable -> log.error("failure " + throwable.getMessage()+ " " + throwable.getCause()));

    return Uni.createFrom().voidItem();
  }
}
