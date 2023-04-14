package cygni.es.exceptions;

public class AggregateNotFoundException extends RuntimeException {
    public AggregateNotFoundException(String msg) {
        super(String.format("aggregate with id %s not found", msg));
    }
}
