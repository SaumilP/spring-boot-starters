/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.model;

/**
 * Represents the outcome of an audited operation.
 *
 * @since 1.0.0
 */
public enum AuditOutcome {

    /**
     * The audited method completed without throwing an exception.
     */
    SUCCESS,

    /**
     * The audited method threw an exception before returning normally.
     */
    FAILURE
}
