/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.config;

import io.github.saumilp.starters.outbox.broker.KafkaBrokerAdapter;
import io.github.saumilp.starters.outbox.broker.MessageBrokerAdapter;
import io.github.saumilp.starters.outbox.broker.RabbitMqBrokerAdapter;
import io.github.saumilp.starters.outbox.publisher.OutboxEventPublisher;
import io.github.saumilp.starters.outbox.relay.OutboxEventRelay;
import io.github.saumilp.starters.outbox.repository.OutboxEventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot auto-configuration for the transactional outbox starter.
 *
 * <p>Registers the following beans when the starter is active:
 * <ul>
 *   <li>{@link KafkaBrokerAdapter} — when {@code spring.outbox.broker=kafka} and
 *       {@code spring-kafka} is on the classpath</li>
 *   <li>{@link RabbitMqBrokerAdapter} — when {@code spring.outbox.broker=rabbitmq} and
 *       {@code spring-rabbit} is on the classpath</li>
 *   <li>{@link OutboxEventPublisher} — always active when the outbox is enabled</li>
 *   <li>{@link OutboxEventRelay} — the {@code @Scheduled} poller</li>
 * </ul>
 *
 * <p>The entire configuration can be disabled via:
 * <pre>{@code spring.outbox.enabled=false}</pre>
 *
 * <p>All beans are annotated with {@link ConditionalOnMissingBean} so consuming applications
 * can replace any individual component by declaring their own bean.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "spring.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    /** Creates the outbox auto-configuration. */
    public OutboxAutoConfiguration() {
    }

    /**
     * Registers the Kafka broker adapter when Kafka is on the classpath and the broker
     * is configured as {@code "kafka"}.
     *
     * @param kafkaTemplate the Kafka template used to publish events; must not be {@code null}
     * @param props         the outbox configuration properties; must not be {@code null}
     * @return a configured {@link KafkaBrokerAdapter}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(MessageBrokerAdapter.class)
    @ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
    @ConditionalOnProperty(prefix = "spring.outbox", name = "broker", havingValue = "kafka", matchIfMissing = true)
    public MessageBrokerAdapter kafkaBrokerAdapter(
            org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties props) {
        return new KafkaBrokerAdapter(kafkaTemplate, props.getKafkaTopicPrefix());
    }

    /**
     * Registers the RabbitMQ broker adapter when Spring AMQP is on the classpath and the
     * broker is configured as {@code "rabbitmq"}.
     *
     * @param rabbitTemplate the Spring AMQP template; must not be {@code null}
     * @param props          the outbox configuration properties; must not be {@code null}
     * @return a configured {@link RabbitMqBrokerAdapter}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(MessageBrokerAdapter.class)
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
    @ConditionalOnProperty(prefix = "spring.outbox", name = "broker", havingValue = "rabbitmq")
    public MessageBrokerAdapter rabbitMqBrokerAdapter(
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate,
            OutboxProperties props) {
        return new RabbitMqBrokerAdapter(rabbitTemplate, props.getRabbitExchange());
    }

    /**
     * Registers the {@link OutboxEventPublisher} for persisting domain events to the outbox.
     *
     * @param repository the JPA repository for outbox events; must not be {@code null}
     * @return a configured {@link OutboxEventPublisher}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(OutboxEventPublisher.class)
    public OutboxEventPublisher outboxEventPublisher(OutboxEventRepository repository) {
        return new OutboxEventPublisher(repository);
    }

    /**
     * Registers the {@link OutboxEventRelay} scheduled poller.
     *
     * @param repository the JPA repository for outbox events; must not be {@code null}
     * @param broker     the active broker adapter; must not be {@code null}
     * @param props      the outbox configuration properties; must not be {@code null}
     * @return a configured {@link OutboxEventRelay}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(OutboxEventRelay.class)
    public OutboxEventRelay outboxEventRelay(OutboxEventRepository repository,
                                              MessageBrokerAdapter broker,
                                              OutboxProperties props) {
        return new OutboxEventRelay(repository, broker, props);
    }
}
