/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.sink;

import io.github.saumilp.starters.auditlog.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An {@link AuditEventSink} that delegates to a list of other sinks in registration order.
 *
 * <p>This is the primary sink registered by the auto-configuration. All beans of type
 * {@link AuditEventSink} discovered in the application context (excluding itself) are
 * injected into this composite. A failure in any individual sink is caught, logged, and
 * swallowed so that remaining sinks still receive the event.
 *
 * @since 1.0.0
 */
public class CompositeAuditEventSink implements AuditEventSink {

    private static final Logger log = LoggerFactory.getLogger(CompositeAuditEventSink.class);

    private final List<AuditEventSink> delegates;

    /**
     * Constructs a composite sink with the given delegate list.
     *
     * @param delegates the ordered list of delegate sinks; must not be {@code null}
     */
    public CompositeAuditEventSink(List<AuditEventSink> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Publishes the event to each delegate in order. Any exception raised by a delegate
     * is caught and logged so that subsequent delegates are still invoked.
     *
     * @param event the audit event to publish; must not be {@code null}
     */
    @Override
    public void publish(AuditEvent event) {
        for (AuditEventSink sink : delegates) {
            try {
                sink.publish(event);
            } catch (Exception ex) {
                log.error("AuditEventSink '{}' failed to publish event for action='{}': {}",
                    sink.getClass().getSimpleName(), event.action(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * Returns the number of delegate sinks registered in this composite.
     *
     * @return the delegate count; always non-negative
     */
    public int delegateCount() {
        return delegates.size();
    }
}
