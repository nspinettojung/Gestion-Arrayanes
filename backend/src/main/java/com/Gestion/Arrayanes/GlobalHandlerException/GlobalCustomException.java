package com.Gestion.Arrayanes.GlobalHandlerException;

import org.springframework.http.HttpStatus;

public class GlobalCustomException extends RuntimeException {

    private final HttpStatus status;

    public GlobalCustomException(String message) {
        this(message, HttpStatus.CONFLICT);
    }

    public GlobalCustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
