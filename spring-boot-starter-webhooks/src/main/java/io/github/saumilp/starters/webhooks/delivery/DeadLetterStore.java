/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import io.github.saumilp.starters.webhooks.model.DeadLetter;
import java.util.List;

/**
 * Records webhook deliveries that exhausted all retry attempts so they can be inspected or replayed.
 *
 * <p>The default {@link InMemoryDeadLetterStore} keeps entries in memory. Applications can supply a
 * durable implementation (database, queue, ...) by declaring their own bean.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public interface DeadLetterStore {

    /**
     * Persists a failed delivery.
     *
     * @param deadLetter the abandoned delivery; must not be {@code null}
     */
    void store(DeadLetter deadLetter);

    /**
     * Returns all recorded dead letters.
     *
     * @return the dead letters; never {@code null}
     */
    List<DeadLetter> all();
}
