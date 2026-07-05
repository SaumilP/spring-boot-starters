/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the outbox starter, bound from the
 * {@code spring.outbox.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   outbox:
 *     enabled: true
 *     relay-interval-ms: 5000
 *     max-retries: 3
 *     broker: kafka
 *     kafka-topic-prefix: "outbox."
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.outbox")
public class OutboxProperties {

    /** Creates an instance with default values. */
    public OutboxProperties() {
    }

    /** Whether the outbox relay is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /**
     * Fixed delay in milliseconds between relay polling cycles.
     * The timer resets after each invocation completes, not from its start.
     * Defaults to {@code 5000} ms.
     */
    private long relayIntervalMs = 5_000L;

    /**
     * Maximum number of relay attempts before an event is marked
     * {@link io.github.saumilp.starters.outbox.model.OutboxEventStatus#FAILED}.
     * Defaults to {@code 3}.
     */
    private int maxRetries = 3;

    /**
     * The message broker type to use. Supported values: {@code "kafka"}, {@code "rabbitmq"}.
     * Determines which {@link io.github.saumilp.starters.outbox.broker.MessageBrokerAdapter}
     * is auto-configured. Defaults to {@code "kafka"}.
     */
    private String broker = "kafka";

    /**
     * Topic name prefix for the Kafka adapter. The full topic name is
     * {@code kafkaTopicPrefix + eventType}. Defaults to {@code "outbox."}.
     */
    private String kafkaTopicPrefix = "outbox.";

    /**
     * RabbitMQ exchange name for the RabbitMQ adapter. Defaults to {@code "outbox"}.
     */
    private String rabbitExchange = "outbox";

    /**
     * Returns whether the outbox relay is enabled.
     *
     * @return {@code true} if the relay is active
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets whether the outbox relay is enabled.
     *
     * @param enabled {@code false} to disable the relay and publisher entirely
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the fixed delay between relay polling cycles in milliseconds.
     *
     * @return the relay interval; always a positive value
     */
    public long getRelayIntervalMs() { return relayIntervalMs; }

    /**
     * Sets the fixed delay between relay polling cycles.
     *
     * @param relayIntervalMs the polling interval in ms; must be positive
     */
    public void setRelayIntervalMs(long relayIntervalMs) { this.relayIntervalMs = relayIntervalMs; }

    /**
     * Returns the maximum number of relay attempts before an event is permanently failed.
     *
     * @return the max retries; always a positive integer
     */
    public int getMaxRetries() { return maxRetries; }

    /**
     * Sets the maximum number of relay attempts.
     *
     * @param maxRetries the maximum retry count; must be positive
     */
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    /**
     * Returns the configured broker type identifier.
     *
     * @return {@code "kafka"} or {@code "rabbitmq"}; never {@code null}
     */
    public String getBroker() { return broker; }

    /**
     * Sets the broker type.
     *
     * @param broker the broker type; {@code "kafka"} or {@code "rabbitmq"}
     */
    public void setBroker(String broker) { this.broker = broker; }

    /**
     * Returns the Kafka topic name prefix.
     *
     * @return the prefix string; never {@code null}
     */
    public String getKafkaTopicPrefix() { return kafkaTopicPrefix; }

    /**
     * Sets the Kafka topic name prefix.
     *
     * @param kafkaTopicPrefix the topic prefix; must not be {@code null}
     */
    public void setKafkaTopicPrefix(String kafkaTopicPrefix) { this.kafkaTopicPrefix = kafkaTopicPrefix; }

    /**
     * Returns the RabbitMQ exchange name.
     *
     * @return the exchange name; never {@code null}
     */
    public String getRabbitExchange() { return rabbitExchange; }

    /**
     * Sets the RabbitMQ exchange name.
     *
     * @param rabbitExchange the exchange name; must not be {@code null} or blank
     */
    public void setRabbitExchange(String rabbitExchange) { this.rabbitExchange = rabbitExchange; }
}
