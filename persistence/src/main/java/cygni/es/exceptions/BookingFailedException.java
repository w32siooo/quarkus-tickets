package cygni.es.exceptions;

public class BookingFailedException extends RuntimeException {

    public BookingFailedException(String msg) {
        super(String.format("Boooking failed because %s ",msg));
    }
}
