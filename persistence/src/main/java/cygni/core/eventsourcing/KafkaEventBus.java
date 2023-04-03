package cygni.core.eventsourcing;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaClientService;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaEventBus implements EventBus {
  private static final Logger logger = Logger.getLogger(KafkaEventBus.class);
  private static final int PUBLISH_TIMEOUT = 1000;
  private static final int BACKOFF_TIMEOUT = 300;
  private static final int RETRY_COUNT = 3;

  @Inject KafkaClientService kafkaClientService;

  @ConfigProperty(name = "mp.messaging.incoming.event-store-in.topic", defaultValue = "event-store")
  String eventStoreTopic;

  public Uni<Void> publish(List<Event> events) {
    final byte[] eventsBytes = SerializerUtils.serializeToJsonBytes(events.toArray(new Event[] {}));
    final ProducerRecord<String, byte[]> record =
        new ProducerRecord<>(eventStoreTopic, eventsBytes);
    logger.infof("publish kafka record value >>>>> %s", new String(record.value()));

    return kafkaClientService
        .<String, byte[]>getProducer("event-store-out")
        .send(record)
        .ifNoItem()
        .after(Duration.ofMillis(PUBLISH_TIMEOUT))
        .fail()
        .onFailure()
        .invoke(Throwable::printStackTrace)
        .onFailure()
        .retry()
        .withBackOff(Duration.of(BACKOFF_TIMEOUT, ChronoUnit.MILLIS))
        .atMost(RETRY_COUNT)
        .onItem()
        .invoke(
            msg ->
                logger.infof(
                    "publish key: %s, value: %s", record.key(), new String(record.value())))
        .replaceWithVoid();
  }
}
