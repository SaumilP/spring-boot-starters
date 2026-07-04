/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.web;

import io.github.saumilp.starters.ratelimit.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler that converts {@link RateLimitExceededException} into a
 * structured HTTP {@code 429 Too Many Requests} response.
 *
 * <p>The response body is a JSON object with the following fields:
 * <pre>{@code
 * {
 *   "status": 429,
 *   "error": "Too Many Requests",
 *   "message": "Rate limit exceeded for key 'rl:192.168.1.1:search': max 60 requests per 60 seconds.",
 *   "retryAfterSeconds": 42,
 *   "timestamp": "2024-07-04T10:15:30Z"
 * }
 * }</pre>
 *
 * <p>The response also includes a {@code Retry-After} header set to the window duration,
 * allowing HTTP clients and intermediaries to implement back-off automatically.
 *
 * <p>This handler is only registered in servlet-based web application contexts. If the consuming
 * application defines its own {@code @ControllerAdvice} that handles
 * {@code RateLimitExceededException}, it takes precedence over this handler.
 *
 * @since 1.0.0
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RateLimitExceptionHandler {

    /**
     * Handles {@link RateLimitExceededException} and returns an HTTP {@code 429} response.
     *
     * @param ex the rate-limit exception containing context about the violated limit;
     *           must not be {@code null}
     * @return a {@link ResponseEntity} with status {@code 429}, a {@code Retry-After} header,
     *         and a JSON error body; never {@code null}
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(RateLimitExceededException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getWindowSeconds()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "status",            HttpStatus.TOO_MANY_REQUESTS.value(),
            "error",             "Too Many Requests",
            "message",           ex.getMessage(),
            "retryAfterSeconds", ex.getWindowSeconds(),
            "timestamp",         Instant.now().toString()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .headers(headers)
            .body(body);
    }
}
