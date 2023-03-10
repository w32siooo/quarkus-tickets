package cygni.producer.processor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import cygni.producer.model.Quote;
import cygni.producer.model.TicketActivatedEvent;
import cygni.producer.model.TicketCreateEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.*;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

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

  @Incoming("ticket-create-request")
  @Retry(maxRetries = 3, delay = 5, delayUnit = ChronoUnit.SECONDS, maxDuration = 1000, durationUnit = ChronoUnit.SECONDS)
  public Uni<Void> consumeCreation(JsonObject command) {

      TicketCreateEvent event = command.mapTo(TicketCreateEvent.class);

    log.info("received event for processing: " + event.toString());

    Future<HttpResponse<Buffer>> future =
              webClient.post(8086,"127.0.0.1","/tickets/create")
                      .sendJson(event);

    log.info("sending event to ticket service: " + event.toString());
    return Uni.createFrom().completionStage(future.toCompletionStage()).map(Unchecked.function(item->{
            if(item.statusCode()==201) {
                return item.bodyAsJsonObject();
            } else {
                throw new RuntimeException("failed to create ticket");
            }
      })).replaceWithVoid()
            ;
  }

  @Incoming("ticket-activate-request")
  @Retry(maxRetries = 3, delay = 5, delayUnit = ChronoUnit.SECONDS, maxDuration = 1000, durationUnit = ChronoUnit.SECONDS)
  public Uni<Void> consumeActivation(JsonObject command) {

    TicketActivatedEvent event = command.mapTo(TicketActivatedEvent.class);


      Future<HttpResponse<Buffer>> future = webClient.post(8086,"127.0.0.1","/tickets/activate")
            .sendJson(event);


    return Uni.createFrom().completionStage(future.toCompletionStage()).map(Unchecked.function(item->{
      if(item.statusCode()==201) {
        return item.bodyAsJsonObject();
      } else {
        throw new RuntimeException("failed to activate ticket");
      }
    })).replaceWithVoid();
  }
 @Incoming("ticket-order-request")
 @Retry(maxRetries = 3, delay = 5, delayUnit = ChronoUnit.SECONDS, maxDuration = 1000, durationUnit = ChronoUnit.SECONDS)
  public Uni<Void> consumeOrder(JsonObject command) {

   TicketActivatedEvent event = command.mapTo(TicketActivatedEvent.class);

   log.error(event.toString());

     Future<HttpResponse<Buffer>> future =  webClient.post(8086, "127.0.0.1", "/tickets/order")
           .sendJson(event);

   return Uni.createFrom().completionStage(future.toCompletionStage()).map(Unchecked.function(item->{
     if(item.statusCode()==201) {
       return item.bodyAsJsonObject();
     } else {
       throw new RuntimeException("failed to order ticket");
     }
   })).replaceWithVoid();
 }
}
