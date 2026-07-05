/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.problem.web;

import io.github.saumilp.starters.problem.config.ProblemDetailsProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Global {@code @RestControllerAdvice} that renders every unhandled exception as an
 * RFC 7807 {@link ProblemDetail} ({@code application/problem+json}).
 *
 * <p>Handles bean-validation failures (with per-field errors), {@link ResponseStatusException}
 * (preserving its status), and a catch-all that maps anything else to {@code 500} without leaking
 * internals. Each response carries a machine-readable {@code code}, a {@code timestamp}, the request
 * path as the {@code instance}, and — when enabled — the current correlation ID.
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ProblemDetailsProperties properties;

    /**
     * Constructs the handler.
     *
     * @param properties the problem-details configuration; must not be {@code null}
     */
    public GlobalExceptionHandler(ProblemDetailsProperties properties) {
        this.properties = properties;
    }

    /**
     * Maps a bean-validation failure on a request body to a {@code 400} problem with field errors.
     *
     * @param ex      the validation exception; must not be {@code null}
     * @param request the current request; must not be {@code null}
     * @return a {@code 400} problem detail with a per-field error list
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = base(HttpStatus.BAD_REQUEST, "Request validation failed",
            "validation-error", request);
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid"))
            .toList();
        problem.setProperty(properties.getFieldErrorsKey(), fieldErrors);
        return problem;
    }

    /**
     * Maps a {@link ResponseStatusException} to a problem detail preserving its status and reason.
     *
     * @param ex      the exception; must not be {@code null}
     * @param request the current request; must not be {@code null}
     * @return a problem detail with the exception's status
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        String detail = StringUtils.hasText(ex.getReason()) ? ex.getReason() : "Request could not be processed";
        return base(status, detail, "request-error", request);
    }

    /**
     * Catch-all mapping anything else to a {@code 500} problem without exposing internals.
     *
     * @param ex      the exception; must not be {@code null}
     * @param request the current request; must not be {@code null}
     * @return a {@code 500} problem detail
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail problem = base(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
            "internal-error", request);
        if (properties.isIncludeStackTrace()) {
            problem.setProperty("trace", stackTraceOf(ex));
        }
        return problem;
    }

    private ProblemDetail base(HttpStatusCode status, String detail, String code, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(titleFor(status));
        problem.setType(typeFor(code));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now().toString());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        if (properties.isIncludeCorrelationId()) {
            String correlationId = MDC.get(properties.getCorrelationMdcKey());
            if (correlationId != null) {
                problem.setProperty("correlationId", correlationId);
            }
        }
        return problem;
    }

    private String titleFor(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.getReasonPhrase() : "Error";
    }

    private URI typeFor(String code) {
        String base = properties.getBaseTypeUri();
        if (!StringUtils.hasText(base) || "about:blank".equals(base)) {
            return URI.create("about:blank");
        }
        String normalized = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return URI.create(normalized + "/" + code);
    }

    private static String stackTraceOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
