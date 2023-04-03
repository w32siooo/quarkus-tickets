package cygni.core.eventsourcing;

import io.smallrye.mutiny.Uni;
import java.util.List;

public interface EventBus {
  Uni<Void> publish(List<Event> events);
}
