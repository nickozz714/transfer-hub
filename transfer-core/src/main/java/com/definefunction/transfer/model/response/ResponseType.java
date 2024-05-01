package com.definefunction.transfer.model.response;

import org.springframework.http.HttpStatus;

public class ResponseType {
    private String message;
    private HttpStatus statusCode;
    private Object object;

    public ResponseType(){}
    public ResponseType(String message, HttpStatus statusCode, Object object) {
        this.message = message;
        this.statusCode = statusCode;
        this.object = object;
    }

    public ResponseType(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
