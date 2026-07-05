/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.model;

import java.util.UUID;

/**
 * An event to be delivered to webhook subscribers.
 *
 * @param id      a unique event identifier (echoed in the {@code X-Webhook-Id} header); must not be blank
 * @param type    the event type (echoed in the {@code X-Webhook-Event} header); must not be blank
 * @param payload the serialized JSON body to POST; must not be {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record WebhookEvent(String id, String type, String payload) {

    /**
     * Canonical constructor validating required fields.
     *
     * @param id      the event identifier; must not be {@code null} or blank
     * @param type    the event type; must not be {@code null} or blank
     * @param payload the JSON payload; must not be {@code null}
     */
    public WebhookEvent {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be null or blank");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
    }

    /**
     * Creates an event with a random identifier.
     *
     * @param type    the event type
     * @param payload the JSON payload
     * @return a new {@link WebhookEvent}; never {@code null}
     */
    public static WebhookEvent of(String type, String payload) {
        return new WebhookEvent(UUID.randomUUID().toString(), type, payload);
    }
}
