/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * An immutable snapshot of an HTTP response cached for idempotency purposes.
 *
 * <p>When a request bearing an {@code Idempotency-Key} header is processed for the first time,
 * the generated response is serialised into this record and stored in the idempotency store.
 * Subsequent requests with the same key within the configured TTL receive the cached response
 * verbatim, without re-executing the handler.
 *
 * <p>Instances are safe to serialise to JSON and store in Redis. All fields are immutable
 * after construction.
 *
 * @param statusCode     the HTTP status code of the original response (e.g. {@code 200})
 * @param headers        a copy of the response headers from the original request; may be empty
 *                       but never {@code null}
 * @param body           the response body as a UTF-8 string; may be empty but never {@code null}
 * @param cachedAt       the instant at which this response was stored; never {@code null}
 * @param idempotencyKey the key that triggered this response; never {@code null} or blank
 *
 * @since 1.0.0
 */
public record CachedResponse(
    int statusCode,
    Map<String, String> headers,
    String body,
    Instant cachedAt,
    String idempotencyKey
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Compact canonical constructor — guards against null fields.
     *
     * @throws IllegalArgumentException if {@code headers}, {@code body}, {@code cachedAt},
     *                                  or {@code idempotencyKey} is {@code null} or blank
     */
    public CachedResponse {
        if (headers == null)
            throw new IllegalArgumentException("headers must not be null");
        if (body == null)
            throw new IllegalArgumentException("body must not be null");
        if (cachedAt == null)
            throw new IllegalArgumentException("cachedAt must not be null");
        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new IllegalArgumentException("idempotencyKey must not be null or blank");
    }
}
