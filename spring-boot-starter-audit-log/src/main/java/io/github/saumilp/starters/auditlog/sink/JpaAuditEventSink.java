/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.sink;

import io.github.saumilp.starters.auditlog.model.AuditEvent;
import io.github.saumilp.starters.auditlog.model.AuditOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * An {@link AuditEventSink} that persists audit events to a relational database using JPA.
 *
 * <p>Each audit event is mapped to an {@link AuditEventEntity} and saved via a
 * {@link AuditEventRepository}. The save is performed in a new transaction
 * ({@link Propagation#REQUIRES_NEW}) so that a rollback of the business transaction does
 * not prevent the audit record from being committed.
 *
 * <p>This sink is activated only when {@code spring-boot-starter-data-jpa} is on the
 * classpath and {@code spring.audit-log.jpa-sink.enabled=true}. The consumer is responsible
 * for creating the {@code audit_events} table — see the README for the Flyway migration script.
 *
 * @since 1.0.0
 */
public class JpaAuditEventSink implements AuditEventSink {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditEventSink.class);

    private final AuditEventRepository repository;

    /**
     * Constructs the sink with the given JPA repository.
     *
     * @param repository the repository used to persist audit events; must not be {@code null}
     */
    public JpaAuditEventSink(AuditEventRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Maps the event to an {@link AuditEventEntity} and saves it in a new transaction.
     * Any persistence exception is caught, logged, and swallowed to prevent audit failures
     * from disrupting the business operation.
     *
     * @param event the audit event to persist; must not be {@code null}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(AuditEvent event) {
        try {
            AuditEventEntity entity = new AuditEventEntity();
            entity.setAction(event.action());
            entity.setResource(event.resource());
            entity.setResourceId(event.resourceId());
            entity.setActor(event.actor());
            entity.setOutcome(event.outcome());
            entity.setErrorMessage(event.errorMessage());
            entity.setOccurredAt(event.occurredAt());
            entity.setDurationMs(event.durationMs());
            repository.save(entity);
        } catch (Exception ex) {
            log.error("Failed to persist audit event for action='{}': {}",
                event.action(), ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // JPA Entity
    // -------------------------------------------------------------------------

    /**
     * JPA entity representing a single persisted audit event in the {@code audit_events} table.
     *
     * <p>The table must be created by the consuming application. See the README for the
     * recommended Flyway migration script.
     *
     * @since 1.0.0
     */
    @Entity
    @Table(name = "audit_events")
    public static class AuditEventEntity {

        /** Surrogate primary key. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** The audited action identifier (e.g., {@code CREATE_ORDER}). */
        @Column(name = "action", nullable = false, length = 100)
        private String action;

        /** The resource type (e.g., {@code Order}). */
        @Column(name = "resource", length = 100)
        private String resource;

        /** The identifier of the affected resource instance. */
        @Column(name = "resource_id", length = 255)
        private String resourceId;

        /** The identity of the actor that initiated the operation. */
        @Column(name = "actor", nullable = false, length = 255)
        private String actor;

        /** Whether the operation succeeded or failed. */
        @Enumerated(EnumType.STRING)
        @Column(name = "outcome", nullable = false, length = 10)
        private AuditOutcome outcome;

        /** The exception message when {@link #outcome} is {@link AuditOutcome#FAILURE}. */
        @Column(name = "error_message", columnDefinition = "TEXT")
        private String errorMessage;

        /** UTC instant at which the event occurred. */
        @Column(name = "occurred_at", nullable = false)
        private Instant occurredAt;

        /** Duration of the intercepted method in milliseconds. */
        @Column(name = "duration_ms", nullable = false)
        private long durationMs;

        /** @return the surrogate primary key */
        public Long getId() { return id; }

        /** @return the action identifier */
        public String getAction() { return action; }
        /** @param action the action identifier */
        public void setAction(String action) { this.action = action; }

        /** @return the resource type */
        public String getResource() { return resource; }
        /** @param resource the resource type */
        public void setResource(String resource) { this.resource = resource; }

        /** @return the resource instance identifier */
        public String getResourceId() { return resourceId; }
        /** @param resourceId the resource instance identifier */
        public void setResourceId(String resourceId) { this.resourceId = resourceId; }

        /** @return the actor identity */
        public String getActor() { return actor; }
        /** @param actor the actor identity */
        public void setActor(String actor) { this.actor = actor; }

        /** @return the operation outcome */
        public AuditOutcome getOutcome() { return outcome; }
        /** @param outcome the operation outcome */
        public void setOutcome(AuditOutcome outcome) { this.outcome = outcome; }

        /** @return the error message, or {@code null} on success */
        public String getErrorMessage() { return errorMessage; }
        /** @param errorMessage the exception message */
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        /** @return the UTC instant the event occurred */
        public Instant getOccurredAt() { return occurredAt; }
        /** @param occurredAt the event instant */
        public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

        /** @return the method duration in milliseconds */
        public long getDurationMs() { return durationMs; }
        /** @param durationMs the method duration */
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    }

    // -------------------------------------------------------------------------
    // Spring Data Repository
    // -------------------------------------------------------------------------

    /**
     * Spring Data JPA repository for {@link AuditEventEntity}.
     *
     * @since 1.0.0
     */
    public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
    }
}
