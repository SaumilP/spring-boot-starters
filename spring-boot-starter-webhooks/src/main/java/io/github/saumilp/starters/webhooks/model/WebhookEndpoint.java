/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.model;

/**
 * A registered outbound webhook subscriber.
 *
 * @param id     a stable identifier for the endpoint; must not be {@code null} or blank
 * @param url    the absolute HTTPS URL to POST events to; must not be {@code null} or blank
 * @param secret the shared secret used to HMAC-sign payloads; must not be {@code null}
 * @param active whether the endpoint currently receives deliveries
 * @author SaumilP
 * @since 1.0.0
 */
public record WebhookEndpoint(String id, String url, String secret, boolean active) {

    /**
     * Canonical constructor validating required fields.
     *
     * @param id     the endpoint identifier; must not be {@code null} or blank
     * @param url    the delivery URL; must not be {@code null} or blank
     * @param secret the signing secret; must not be {@code null}
     * @param active whether the endpoint is active
     */
    public WebhookEndpoint {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be null or blank");
        }
        if (secret == null) {
            throw new IllegalArgumentException("secret must not be null");
        }
    }

    /**
     * Creates an active endpoint.
     *
     * @param id     the endpoint identifier
     * @param url    the delivery URL
     * @param secret the signing secret
     * @return an active {@link WebhookEndpoint}; never {@code null}
     */
    public static WebhookEndpoint active(String id, String url, String secret) {
        return new WebhookEndpoint(id, url, secret, true);
    }
}
