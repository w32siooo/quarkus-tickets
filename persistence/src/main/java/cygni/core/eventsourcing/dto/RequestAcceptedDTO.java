package cygni.core.eventsourcing.dto;

import java.util.UUID;

public record RequestAcceptedDTO(String message, UUID aggregateId){
}
