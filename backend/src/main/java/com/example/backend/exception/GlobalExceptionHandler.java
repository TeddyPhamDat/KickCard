package com.example.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        body.put("message", "Access denied");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failure for request {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", "Authentication required");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, HttpServletRequest request) {
        // Log full stacktrace to server logs
        log.error("Unhandled exception for request {} {}", request.getMethod(), request.getRequestURI(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
