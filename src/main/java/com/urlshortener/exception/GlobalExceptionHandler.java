package com.urlshortener.exception;

import com.urlshortener.dto.UrlDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShortCodeNotFoundException.class)
    public ResponseEntity<UrlDtos.ErrorResponse> handleNotFound(ShortCodeNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<UrlDtos.ErrorResponse> handleExpired(UrlExpiredException ex) {
        return buildError(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(ShortCodeConflictException.class)
    public ResponseEntity<UrlDtos.ErrorResponse> handleConflict(ShortCodeConflictException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UrlDtos.ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UrlDtos.ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<UrlDtos.ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
            UrlDtos.ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
}
