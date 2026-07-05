/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.sink;

import io.github.saumilp.starters.auditlog.model.AuditEvent;
import io.github.saumilp.starters.auditlog.model.AuditOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AuditEventSink} that writes audit events to the application log using SLF4J.
 *
 * <p>Successful events are logged at {@code INFO} level; failures are logged at {@code WARN}
 * level and include the error message. The log format is structured for easy parsing by
 * log aggregators such as Elasticsearch or Splunk.
 *
 * <p>This sink is always active and serves as the default unless the consuming application
 * sets {@code spring.audit-log.logging-sink.enabled=false}.
 *
 * @since 1.0.0
 */
public class LoggingAuditEventSink implements AuditEventSink {

    /** Creates a new logging sink. */
    public LoggingAuditEventSink() {
    }

    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    /**
     * {@inheritDoc}
     *
     * <p>Logs the event at {@code INFO} level on success or {@code WARN} level on failure,
     * including action, resource, resourceId, actor, outcome, duration, and error message
     * (on failure).
     *
     * @param event the audit event to log; must not be {@code null}
     */
    @Override
    public void publish(AuditEvent event) {
        if (event.outcome() == AuditOutcome.FAILURE) {
            log.warn("AUDIT action={} resource={} resourceId={} actor={} outcome={} durationMs={} error={}",
                event.action(), event.resource(), event.resourceId(), event.actor(),
                event.outcome(), event.durationMs(), event.errorMessage());
        } else {
            log.info("AUDIT action={} resource={} resourceId={} actor={} outcome={} durationMs={}",
                event.action(), event.resource(), event.resourceId(), event.actor(),
                event.outcome(), event.durationMs());
        }
    }
}
