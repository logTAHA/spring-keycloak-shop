package com.shop.handler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("Business exception on {} {}: {} -> {}",
                request.getMethod(), request.getRequestURI(),
                ex.status(), ex.getMessage()
        );
        return problem(ex.status(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> errorsByField = ex.getBindingResult().getFieldErrors().stream()
                .collect(
                        Collectors.groupingBy(
                                FieldError::getField,
                                LinkedHashMap::new,
                                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                        )
                );

        List<Map<String, List<String>>> errors = errorsByField.entrySet().stream()
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .toList();

        log.warn("Validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), errorsByField.keySet());
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed", request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        log.info("No resource for {} {}", request.getMethod(), request.getRequestURI());
        return problem(HttpStatus.NOT_FOUND, "The requested resource does not exist", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method {} not allowed on {}", ex.getMethod(), request.getRequestURI());
        return problem(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body on {} {}", request.getMethod(), request.getRequestURI());
        return problem(HttpStatus.BAD_REQUEST, "Request body is missing", request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) throws Exception {
        // security exceptions must reach the security filter chain, not become a 500 here
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
