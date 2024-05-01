package com.definefunction.transfer.exception;

public class TransferRecordNotValidException extends RuntimeException{

    public TransferRecordNotValidException(String message) {
        super(message);
    }

    public TransferRecordNotValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
