/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.model;

/**
 * Lifecycle status of an {@link OutboxEvent}.
 *
 * <p>Events start as {@link #PENDING} when first persisted. The
 * {@link io.github.saumilp.starters.outbox.relay.OutboxEventRelay} transitions them to
 * {@link #PROCESSED} on successful broker delivery, or to {@link #FAILED} when all retry
 * attempts are exhausted.
 *
 * @since 1.0.0
 */
public enum OutboxEventStatus {

    /**
     * The event has been persisted to the outbox table and is waiting to be forwarded
     * to the message broker by the relay poller.
     */
    PENDING,

    /**
     * The event was successfully delivered to the message broker. Terminal state.
     */
    PROCESSED,

    /**
     * The event could not be delivered after exhausting all retry attempts. Terminal state.
     * The {@code error_message} column on the row contains the last failure reason.
     */
    FAILED
}
