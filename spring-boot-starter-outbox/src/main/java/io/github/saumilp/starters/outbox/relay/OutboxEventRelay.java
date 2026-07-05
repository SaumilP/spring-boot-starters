/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.relay;

import io.github.saumilp.starters.outbox.broker.MessageBrokerAdapter;
import io.github.saumilp.starters.outbox.config.OutboxProperties;
import io.github.saumilp.starters.outbox.model.OutboxEvent;
import io.github.saumilp.starters.outbox.model.OutboxEventStatus;
import io.github.saumilp.starters.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled component that polls the outbox table for pending events and forwards them to
 * the configured {@link MessageBrokerAdapter}.
 *
 * <p>The relay runs at a fixed delay defined by {@code spring.outbox.relay-interval-ms}
 * (default: 5 000 ms). On each tick it:
 * <ol>
 *   <li>Fetches all {@link OutboxEventStatus#PENDING} events whose retry count is below
 *       {@link OutboxProperties#getMaxRetries()}, ordered oldest-first.</li>
 *   <li>Calls {@link MessageBrokerAdapter#send} for each event.</li>
 *   <li>On success — marks the event {@link OutboxEventStatus#PROCESSED} and sets
 *       {@link OutboxEvent#setProcessedAt(Instant)}.</li>
 *   <li>On failure — increments {@link OutboxEvent#setRetryCount(int)} and logs a warning.
 *       When {@code retryCount >= maxRetries}, marks the event
 *       {@link OutboxEventStatus#FAILED} and logs an error.</li>
 *   <li>Saves the updated event back to the database.</li>
 * </ol>
 *
 * <p>Each relay tick runs in its own transaction so that a broker failure for one event
 * does not roll back the status updates already made for previously processed events in
 * the same batch.
 *
 * @since 1.0.0
 */
public class OutboxEventRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventRelay.class);

    private final OutboxEventRepository repository;
    private final MessageBrokerAdapter broker;
    private final OutboxProperties props;

    /**
     * Constructs the relay with its required collaborators.
     *
     * @param repository the JPA repository used to query and update outbox events;
     *                   must not be {@code null}
     * @param broker     the broker adapter used to forward events; must not be {@code null}
     * @param props      the starter configuration properties; must not be {@code null}
     */
    public OutboxEventRelay(OutboxEventRepository repository,
                            MessageBrokerAdapter broker,
                            OutboxProperties props) {
        this.repository = repository;
        this.broker     = broker;
        this.props      = props;
    }

    /**
     * Polls the outbox table and relays pending events to the message broker.
     *
     * <p>Triggered automatically by the Spring scheduler at the interval configured via
     * {@code spring.outbox.relay-interval-ms}. Each invocation runs within its own
     * transaction.
     */
    @Scheduled(fixedDelayString = "${spring.outbox.relay-interval-ms:5000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> pending = repository
            .findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING, props.getMaxRetries());

        for (OutboxEvent event : pending) {
            try {
                broker.send(event);
                event.setStatus(OutboxEventStatus.PROCESSED);
                event.setProcessedAt(Instant.now());
                log.debug("Relayed outbox event id={} type={}", event.getId(), event.getEventType());
            } catch (Exception ex) {
                int newCount = event.getRetryCount() + 1;
                event.setRetryCount(newCount);
                event.setErrorMessage(ex.getMessage());
                if (newCount >= props.getMaxRetries()) {
                    event.setStatus(OutboxEventStatus.FAILED);
                    log.error("Outbox event id={} permanently failed after {} retries: {}",
                        event.getId(), props.getMaxRetries(), ex.getMessage(), ex);
                } else {
                    log.warn("Outbox event id={} relay failed (attempt {}): {}",
                        event.getId(), newCount, ex.getMessage());
                }
            }
            repository.save(event);
        }
    }
}
