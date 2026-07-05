/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.broker;

import io.github.saumilp.starters.outbox.model.OutboxEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * {@link MessageBrokerAdapter} implementation that forwards outbox events to RabbitMQ.
 *
 * <p>Each event is published to the configured exchange with
 * {@link OutboxEvent#getEventType()} as the routing key. Consumers bind queues to the
 * exchange using routing-key patterns that match the event types they are interested in.
 *
 * <p>Example: with exchange {@code "outbox"} and event type {@code "ORDER_CREATED"},
 * the message is published to exchange {@code "outbox"} with routing key
 * {@code "ORDER_CREATED"}.
 *
 * @since 1.0.0
 */
public class RabbitMqBrokerAdapter implements MessageBrokerAdapter {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    /**
     * Constructs the adapter with the given RabbitMQ template and exchange name.
     *
     * @param rabbitTemplate the Spring AMQP rabbit template; must not be {@code null}
     * @param exchange       the RabbitMQ exchange to publish events to;
     *                       must not be {@code null} or blank
     */
    public RabbitMqBrokerAdapter(RabbitTemplate rabbitTemplate, String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange       = exchange;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Publishes the event payload to the configured exchange with the event type as the
     * routing key. The call delegates to {@link RabbitTemplate#convertAndSend} which is
     * synchronous by default when publisher confirms are not enabled.
     *
     * @param event the outbox event to forward; must not be {@code null}
     * @throws Exception if the RabbitMQ send fails
     */
    @Override
    public void send(OutboxEvent event) throws Exception {
        rabbitTemplate.convertAndSend(exchange, event.getEventType(), event.getPayload());
    }
}
