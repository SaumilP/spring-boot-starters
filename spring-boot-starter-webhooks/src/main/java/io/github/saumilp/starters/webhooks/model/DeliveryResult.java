/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.model;

/**
 * The outcome of delivering a {@link WebhookEvent} to a single {@link WebhookEndpoint}.
 *
 * @param endpointId the target endpoint id; never {@code null}
 * @param delivered  whether the endpoint accepted the event (2xx)
 * @param attempts   the number of attempts made (including the successful one)
 * @param detail     a status or error description; may be {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record DeliveryResult(String endpointId, boolean delivered, int attempts, String detail) {

    /**
     * Creates a successful result.
     *
     * @param endpointId the endpoint id
     * @param attempts   the number of attempts made
     * @return a delivered result; never {@code null}
     */
    public static DeliveryResult delivered(String endpointId, int attempts) {
        return new DeliveryResult(endpointId, true, attempts, null);
    }

    /**
     * Creates a failed result.
     *
     * @param endpointId the endpoint id
     * @param attempts   the number of attempts made
     * @param detail     the failure reason
     * @return a failed result; never {@code null}
     */
    public static DeliveryResult failed(String endpointId, int attempts, String detail) {
        return new DeliveryResult(endpointId, false, attempts, detail);
    }
}
