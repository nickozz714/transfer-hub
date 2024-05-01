package com.definefunction.transfer.exception;

public class UserNotAuthorizedException extends RuntimeException{

    public UserNotAuthorizedException(String message) {
        super(message);
    }

    public UserNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
