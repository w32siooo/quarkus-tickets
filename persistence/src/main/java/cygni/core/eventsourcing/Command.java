package cygni.core.eventsourcing;

public interface Command {
  String getAggregateId();
}
