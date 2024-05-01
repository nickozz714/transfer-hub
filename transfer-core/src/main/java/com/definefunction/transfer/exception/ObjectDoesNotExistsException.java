package com.definefunction.transfer.exception;

public class ObjectDoesNotExistsException extends RuntimeException {

    public ObjectDoesNotExistsException(String message) {
        super(message);
    }

    public ObjectDoesNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
