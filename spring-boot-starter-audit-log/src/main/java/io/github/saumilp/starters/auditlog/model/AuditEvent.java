/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.model;

import java.time.Instant;

/**
 * An immutable record representing a single auditable event captured by the audit-log aspect.
 *
 * <p>Instances are created by the aspect after each intercepted method invocation and passed
 * to all registered {@link io.github.saumilp.starters.auditlog.sink.AuditEventSink}
 * implementations for persistence or forwarding.
 *
 * @param action       upper-snake-case identifier for the audited operation
 *                     (e.g., {@code "CREATE_ORDER"}); never {@code null}
 * @param resource     the type of resource affected (e.g., {@code "Order"});
 *                     may be empty, never {@code null}
 * @param resourceId   the identifier of the affected resource instance;
 *                     may be {@code null} when not applicable or not resolvable
 * @param actor        the identity of the principal that initiated the operation;
 *                     may be {@code "anonymous"} for unauthenticated requests
 * @param outcome      whether the operation succeeded or failed; never {@code null}
 * @param errorMessage the exception message if {@code outcome} is
 *                     {@link AuditOutcome#FAILURE}; {@code null} on success
 * @param occurredAt   the UTC instant at which the event was captured; never {@code null}
 * @param durationMs   the time taken by the intercepted method in milliseconds;
 *                     always non-negative
 *
 * @since 1.0.0
 */
public record AuditEvent(
    String action,
    String resource,
    String resourceId,
    String actor,
    AuditOutcome outcome,
    String errorMessage,
    Instant occurredAt,
    long durationMs
) {
    /**
     * Compact canonical constructor — guards required non-null fields.
     *
     * @throws IllegalArgumentException if {@code action}, {@code resource}, {@code outcome},
     *                                  or {@code occurredAt} is {@code null}
     */
    public AuditEvent {
        if (action     == null) throw new IllegalArgumentException("action must not be null");
        if (resource   == null) throw new IllegalArgumentException("resource must not be null");
        if (outcome    == null) throw new IllegalArgumentException("outcome must not be null");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
    }
}
