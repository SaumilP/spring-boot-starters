/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.model;

import java.time.Instant;

/**
 * A record of a webhook delivery that exhausted all retry attempts.
 *
 * @param endpointId the target endpoint id; never {@code null}
 * @param eventId    the undelivered event id; never {@code null}
 * @param eventType  the undelivered event type; never {@code null}
 * @param payload    the JSON payload that failed to deliver; never {@code null}
 * @param reason     why delivery ultimately failed; may be {@code null}
 * @param failedAt   when the delivery was abandoned; never {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record DeadLetter(
        String endpointId,
        String eventId,
        String eventType,
        String payload,
        String reason,
        Instant failedAt) {
}
