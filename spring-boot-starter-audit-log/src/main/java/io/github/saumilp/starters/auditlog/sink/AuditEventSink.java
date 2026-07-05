/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.sink;

import io.github.saumilp.starters.auditlog.model.AuditEvent;

/**
 * Strategy interface for persisting or forwarding audit events.
 *
 * <p>Multiple implementations may be registered in the application context. The
 * {@link CompositeAuditEventSink} collects all registered sinks and delegates to each
 * in order. Consuming applications can add custom sinks (e.g., to forward events to
 * Kafka or a remote audit service) by declaring beans that implement this interface.
 *
 * <p>Implementations should be idempotent and must not throw checked exceptions —
 * failures should be logged and swallowed to avoid disrupting the business operation
 * that triggered the audit event.
 *
 * @since 1.0.0
 */
public interface AuditEventSink {

    /**
     * Processes the given audit event.
     *
     * <p>This method is called synchronously after the intercepted method completes.
     * Long-running or I/O-bound sinks should delegate to an asynchronous executor to
     * avoid adding latency to the request path.
     *
     * @param event the captured audit event; never {@code null}
     */
    void publish(AuditEvent event);
}
