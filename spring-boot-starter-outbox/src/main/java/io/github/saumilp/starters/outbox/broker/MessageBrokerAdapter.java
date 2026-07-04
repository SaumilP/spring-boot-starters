/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.broker;

import io.github.saumilp.starters.outbox.model.OutboxEvent;

/**
 * Abstraction over message broker implementations used by the outbox relay.
 *
 * <p>Implementations must be <em>idempotent</em> — the relay may invoke {@link #send} more
 * than once for the same event in edge cases such as a JVM crash between successful broker
 * delivery and the subsequent status update. Consumers should use
 * {@link OutboxEvent#getId()} as a deduplication key.
 *
 * <p>Two implementations are provided out of the box:
 * <ul>
 *   <li>{@link KafkaBrokerAdapter} — routes events to Kafka topics based on event type</li>
 *   <li>{@link RabbitMqBrokerAdapter} — publishes events to a RabbitMQ exchange with the
 *       event type as the routing key</li>
 * </ul>
 *
 * <p>Consuming applications can replace either by declaring a {@code MessageBrokerAdapter}
 * bean in their application context.
 *
 * @since 1.0.0
 */
public interface MessageBrokerAdapter {

    /**
     * Sends the given outbox event to the target broker destination.
     *
     * <p>Implementations should block until the broker acknowledges receipt (e.g., call
     * {@code Future.get()} on Kafka's send result) to ensure the relay can safely mark the
     * event as processed after this method returns.
     *
     * @param event the outbox event to forward; must not be {@code null}
     * @throws Exception if delivery fails; the relay will increment the retry counter and
     *                   re-attempt on the next polling cycle
     */
    void send(OutboxEvent event) throws Exception;
}
