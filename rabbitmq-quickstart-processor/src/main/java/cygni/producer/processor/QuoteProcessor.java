package cygni.producer.processor;

import javax.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import io.smallrye.reactive.messaging.annotations.Blocking;
/**
 * A bean consuming data from the "quote-requests" RabbitMQ queue and giving out a random quote.
 * The result is pushed to the "quotes" RabbitMQ exchange.
 */
@ApplicationScoped
@Slf4j
public class QuoteProcessor {
    @Incoming("requests")
    @Blocking
   @Outgoing("quotes")
   public String consume(String quote){
      log.error("consumed from requests, processing and putting on quotes");

      return quote+" appended";
   }

}