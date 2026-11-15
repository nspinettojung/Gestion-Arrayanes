package com.Gestion.Arrayanes.GlobalHandlerException;

import org.springframework.http.HttpStatus;

public class BadRequestException extends GlobalCustomException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
