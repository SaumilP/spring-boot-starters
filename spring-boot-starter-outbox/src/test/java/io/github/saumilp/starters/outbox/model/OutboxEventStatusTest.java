/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OutboxEventStatus} enum values and defaults.
 */
class OutboxEventStatusTest {

    @Test
    void should_containAllThreeValues() {
        OutboxEventStatus[] values = OutboxEventStatus.values();
        assertThat(values).containsExactlyInAnyOrder(
            OutboxEventStatus.PENDING,
            OutboxEventStatus.PROCESSED,
            OutboxEventStatus.FAILED
        );
    }

    @Test
    void should_resolveByName_when_givenValidString() {
        assertThat(OutboxEventStatus.valueOf("PENDING")).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(OutboxEventStatus.valueOf("PROCESSED")).isEqualTo(OutboxEventStatus.PROCESSED);
        assertThat(OutboxEventStatus.valueOf("FAILED")).isEqualTo(OutboxEventStatus.FAILED);
    }

    @Test
    void should_setPendingAsInitialStatus_when_entityCreated() {
        OutboxEvent event = new OutboxEvent();
        event.onCreate();
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
    }
}
