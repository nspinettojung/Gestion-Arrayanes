package com.Gestion.Arrayanes.GlobalHandlerException;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends GlobalCustomException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
