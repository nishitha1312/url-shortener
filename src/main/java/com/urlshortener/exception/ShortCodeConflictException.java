package com.urlshortener.exception;

public class ShortCodeConflictException extends RuntimeException {
    public ShortCodeConflictException(String message) {
        super(message);
    }
}
