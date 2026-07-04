/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AuditEvent} record construction and validation.
 */
class AuditEventTest {

    @Test
    void should_createEvent_when_allRequiredFieldsPresent() {
        AuditEvent event = new AuditEvent("CREATE_ORDER", "Order", "42",
            "alice", AuditOutcome.SUCCESS, null, Instant.now(), 120L);
        assertThat(event.action()).isEqualTo("CREATE_ORDER");
        assertThat(event.resource()).isEqualTo("Order");
        assertThat(event.resourceId()).isEqualTo("42");
        assertThat(event.actor()).isEqualTo("alice");
        assertThat(event.outcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(event.durationMs()).isEqualTo(120L);
        assertThat(event.errorMessage()).isNull();
    }

    @Test
    void should_throwException_when_actionNull() {
        assertThatThrownBy(() ->
            new AuditEvent(null, "Order", null, "alice", AuditOutcome.SUCCESS, null, Instant.now(), 0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("action");
    }

    @Test
    void should_throwException_when_resourceNull() {
        assertThatThrownBy(() ->
            new AuditEvent("ACTION", null, null, "alice", AuditOutcome.SUCCESS, null, Instant.now(), 0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("resource");
    }

    @Test
    void should_throwException_when_outcomeNull() {
        assertThatThrownBy(() ->
            new AuditEvent("ACTION", "Order", null, "alice", null, null, Instant.now(), 0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("outcome");
    }

    @Test
    void should_throwException_when_occurredAtNull() {
        assertThatThrownBy(() ->
            new AuditEvent("ACTION", "Order", null, "alice", AuditOutcome.SUCCESS, null, null, 0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("occurredAt");
    }

    @Test
    void should_allowNullResourceId_when_notApplicable() {
        AuditEvent event = new AuditEvent("DELETE_USER", "User", null,
            "admin", AuditOutcome.SUCCESS, null, Instant.now(), 50L);
        assertThat(event.resourceId()).isNull();
    }

    @Test
    void should_captureErrorMessage_when_failureOutcome() {
        AuditEvent event = new AuditEvent("LOGIN", "Session", null,
            "bob", AuditOutcome.FAILURE, "Bad credentials", Instant.now(), 10L);
        assertThat(event.outcome()).isEqualTo(AuditOutcome.FAILURE);
        assertThat(event.errorMessage()).isEqualTo("Bad credentials");
    }
}
