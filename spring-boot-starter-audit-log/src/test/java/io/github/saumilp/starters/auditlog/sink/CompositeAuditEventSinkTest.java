/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.sink;

import io.github.saumilp.starters.auditlog.model.AuditEvent;
import io.github.saumilp.starters.auditlog.model.AuditOutcome;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CompositeAuditEventSink} delegation and fault isolation.
 */
class CompositeAuditEventSinkTest {

    private AuditEvent sampleEvent() {
        return new AuditEvent("TEST_ACTION", "Resource", "1",
            "alice", AuditOutcome.SUCCESS, null, Instant.now(), 5L);
    }

    @Test
    void should_publishToAllDelegates_when_allSucceed() {
        List<String> received = new ArrayList<>();
        AuditEventSink s1 = e -> received.add("s1");
        AuditEventSink s2 = e -> received.add("s2");
        CompositeAuditEventSink composite = new CompositeAuditEventSink(List.of(s1, s2));
        composite.publish(sampleEvent());
        assertThat(received).containsExactly("s1", "s2");
    }

    @Test
    void should_continueToNextDelegate_when_oneFails() {
        List<String> received = new ArrayList<>();
        AuditEventSink failing = e -> { throw new RuntimeException("sink error"); };
        AuditEventSink ok      = e -> received.add("ok");
        CompositeAuditEventSink composite = new CompositeAuditEventSink(List.of(failing, ok));
        composite.publish(sampleEvent());
        assertThat(received).containsExactly("ok");
    }

    @Test
    void should_reportCorrectDelegateCount() {
        CompositeAuditEventSink composite = new CompositeAuditEventSink(
            List.of(e -> {}, e -> {}, e -> {}));
        assertThat(composite.delegateCount()).isEqualTo(3);
    }

    @Test
    void should_handleEmptyDelegateList_without_error() {
        CompositeAuditEventSink composite = new CompositeAuditEventSink(List.of());
        composite.publish(sampleEvent());
        assertThat(composite.delegateCount()).isZero();
    }
}
