/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.repository;

import io.github.saumilp.starters.outbox.model.OutboxEvent;
import io.github.saumilp.starters.outbox.model.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link OutboxEvent} entities.
 *
 * <p>Provides derived query methods used by the
 * {@link io.github.saumilp.starters.outbox.relay.OutboxEventRelay} to efficiently
 * fetch batches of pending events for relay processing.
 *
 * @since 1.0.0
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Returns all events with the given status, ordered oldest-first.
     *
     * @param status the lifecycle status to filter on; must not be {@code null}
     * @return an ordered list of matching events; never {@code null}
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    /**
     * Returns all events with the given status whose retry count is below the specified
     * maximum, ordered oldest-first. Used by the relay to skip permanently failed events.
     *
     * @param status     the lifecycle status to filter on; must not be {@code null}
     * @param maxRetries the exclusive upper bound on {@code retryCount}; must be positive
     * @return an ordered list of matching events; never {@code null}
     */
    List<OutboxEvent> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
        OutboxEventStatus status, int maxRetries);
}
