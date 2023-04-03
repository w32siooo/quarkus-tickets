package cygni.core.eventsourcing;

import java.util.UUID;

public interface Aggregate {

    UUID getId();

    String getType();


}
