/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an auditable operation. When applied to a Spring-managed bean method,
 * the audit-log aspect intercepts the invocation and publishes an {@link
 * io.github.saumilp.starters.auditlog.model.AuditEvent} to all registered
 * {@link io.github.saumilp.starters.auditlog.sink.AuditEventSink} implementations.
 *
 * <p>The event is published <em>after</em> method completion. If the method throws an
 * exception the event is still published with
 * {@link io.github.saumilp.starters.auditlog.model.AuditOutcome#FAILURE} so that failed
 * attempts are also traceable.
 *
 * <p>Example:
 * <pre>{@code
 * @Audited(action = "USER_LOGIN", resource = "User")
 * public AuthToken login(String username, String password) { ... }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {

    /**
     * A short, upper-snake-case identifier for the audited action
     * (e.g., {@code "CREATE_ORDER"}, {@code "DELETE_USER"}).
     * Defaults to the method name when empty.
     *
     * @return the action name; may be empty to use the method name
     */
    String action() default "";

    /**
     * The type of resource being acted upon (e.g., {@code "Order"}, {@code "User"}).
     * Used to group audit events by domain object.
     *
     * @return the resource type label; may be empty
     */
    String resource() default "";

    /**
     * Optional parameter-name reference (prefixed with {@code #}) evaluated against the
     * method arguments to extract the resource identifier
     * (e.g., {@code "#orderId"}, {@code "#request.userId"}).
     * If empty, no resource ID is recorded.
     *
     * @return the parameter reference expression for the resource ID; may be empty
     */
    String resourceIdExpression() default "";
}
