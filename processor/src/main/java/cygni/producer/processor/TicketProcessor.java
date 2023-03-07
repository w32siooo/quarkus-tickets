package cygni.producer.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import cygni.producer.model.TicketActivatedEvent;
import cygni.producer.model.TicketOrderEvent;
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
  public Uni<Void> consumeOrder(JsonObject command) {

      TicketOrderEvent event = command.mapTo(TicketOrderEvent.class);

    log.error(event.toString());

    webClient.post(8085,"127.0.0.1","/tickets/create")
            .sendJson(event)
            .onFailure(throwable -> log.error("failure " + throwable.getMessage()+ " " + throwable.getCause()));

    return Uni.createFrom().voidItem();
  }

  @Incoming("ticket-activations")
  public Uni<Void> consumeActivation(JsonObject command) {

    TicketActivatedEvent event = command.mapTo(TicketActivatedEvent.class);

    log.error(event.toString());

    webClient.post(8085,"127.0.0.1","/tickets/activate")
            .sendJson(event)
            .onFailure(throwable -> log.error("failure " + throwable.getMessage()+ " " + throwable.getCause()));

    return Uni.createFrom().voidItem();
  }
}
