/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.model;

/**
 * The outcome of a single {@link NotificationMessage} send attempt.
 *
 * @param success           whether the provider accepted the message
 * @param channel           the channel the message was sent on; never {@code null}
 * @param providerMessageId the provider-assigned message identifier, when accepted; may be {@code null}
 * @param detail            a human-readable status or error description; may be {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record NotificationResult(
        boolean success,
        Channel channel,
        String providerMessageId,
        String detail) {

    /**
     * Creates a successful result.
     *
     * @param channel           the channel the message was sent on; must not be {@code null}
     * @param providerMessageId the provider-assigned message identifier; may be {@code null}
     * @return a success result; never {@code null}
     */
    public static NotificationResult success(Channel channel, String providerMessageId) {
        return new NotificationResult(true, channel, providerMessageId, null);
    }

    /**
     * Creates a failure result.
     *
     * @param channel the channel that was attempted; must not be {@code null}
     * @param detail  the failure reason; may be {@code null}
     * @return a failure result; never {@code null}
     */
    public static NotificationResult failure(Channel channel, String detail) {
        return new NotificationResult(false, channel, null, detail);
    }
}
