/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import io.github.saumilp.starters.webhooks.model.DeadLetter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A thread-safe, in-memory {@link DeadLetterStore}.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class InMemoryDeadLetterStore implements DeadLetterStore {

    private final List<DeadLetter> deadLetters = new CopyOnWriteArrayList<>();

    /** Creates an empty store. */
    public InMemoryDeadLetterStore() {
    }

    @Override
    public void store(DeadLetter deadLetter) {
        deadLetters.add(deadLetter);
    }

    @Override
    public List<DeadLetter> all() {
        return List.copyOf(deadLetters);
    }
}
