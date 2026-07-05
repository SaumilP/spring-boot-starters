/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.broker;

import io.github.saumilp.starters.outbox.model.OutboxEvent;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * {@link MessageBrokerAdapter} implementation that forwards outbox events to Apache Kafka.
 *
 * <p>Each event is sent to a topic named {@code <topicPrefix><eventType>}. For example,
 * with a prefix of {@code "outbox."} and an event type of {@code "ORDER_CREATED"}, the
 * event is sent to the topic {@code "outbox.ORDER_CREATED"}.
 *
 * <p>The {@link OutboxEvent#getAggregateId()} is used as the Kafka message key, which
 * guarantees that all events for the same aggregate instance are assigned to the same
 * partition — preserving per-aggregate ordering.
 *
 * <p>The send is synchronous ({@code .get()}) so the relay can safely mark the event as
 * processed after this method returns without risk of data loss.
 *
 * @since 1.0.0
 */
public class KafkaBrokerAdapter implements MessageBrokerAdapter {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicPrefix;

    /**
     * Constructs the adapter with the given Kafka template and topic prefix.
     *
     * @param kafkaTemplate the Spring Kafka template; must not be {@code null}
     * @param topicPrefix   the prefix prepended to the event type to form the topic name;
     *                      must not be {@code null} (use empty string for no prefix)
     */
    public KafkaBrokerAdapter(KafkaTemplate<String, String> kafkaTemplate, String topicPrefix) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicPrefix   = topicPrefix;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends the event payload to topic {@code <topicPrefix><eventType>} with
     * {@link OutboxEvent#getAggregateId()} as the partition key. Blocks until the broker
     * acknowledges the write.
     *
     * @param event the outbox event to forward; must not be {@code null}
     * @throws Exception if the Kafka send fails or the future is interrupted
     */
    @Override
    public void send(OutboxEvent event) throws Exception {
        String topic = topicPrefix + event.getEventType();
        kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();
    }
}
