package com.shop.handler;

import org.springframework.http.HttpStatus;

/**
 * Base exception for business failures; carries the HTTP status directly.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
