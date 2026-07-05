/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.aspect;

import io.github.saumilp.starters.auditlog.actor.ActorResolver;
import io.github.saumilp.starters.auditlog.annotation.Audited;
import io.github.saumilp.starters.auditlog.model.AuditEvent;
import io.github.saumilp.starters.auditlog.model.AuditOutcome;
import io.github.saumilp.starters.auditlog.sink.AuditEventSink;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * AOP aspect that intercepts methods annotated with {@link Audited} and publishes
 * {@link AuditEvent} instances to all registered {@link AuditEventSink} implementations.
 *
 * <p>The aspect records:
 * <ul>
 *   <li>The action (from {@link Audited#action()} or the method name as fallback)</li>
 *   <li>The resource type (from {@link Audited#resource()})</li>
 *   <li>The resource ID resolved from {@link Audited#resourceIdExpression()} by matching
 *       the parameter name in the method signature</li>
 *   <li>The actor (from the injected {@link ActorResolver})</li>
 *   <li>The outcome ({@link AuditOutcome#SUCCESS} or {@link AuditOutcome#FAILURE})</li>
 *   <li>The error message on failure</li>
 *   <li>The method duration in milliseconds</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Aspect
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditEventSink sink;
    private final ActorResolver actorResolver;

    /**
     * Constructs the aspect with the primary sink and actor resolver.
     *
     * @param sink          the sink to publish events to (typically a
     *                      {@link io.github.saumilp.starters.auditlog.sink.CompositeAuditEventSink});
     *                      must not be {@code null}
     * @param actorResolver the strategy for resolving the current actor; must not be {@code null}
     */
    public AuditLogAspect(AuditEventSink sink, ActorResolver actorResolver) {
        this.sink          = sink;
        this.actorResolver = actorResolver;
    }

    /**
     * Intercepts invocations of methods annotated with {@link Audited}, measures their
     * duration, and publishes an {@link AuditEvent} regardless of whether the method
     * succeeded or threw.
     *
     * @param pjp     the proceeding join point; must not be {@code null}
     * @param audited the annotation from the intercepted method; must not be {@code null}
     * @return the return value of the intercepted method
     * @throws Throwable re-throws any exception raised by the method
     */
    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        String action   = audited.action().isEmpty() ? method.getName() : audited.action();
        String resource = audited.resource();
        String actor    = safeResolveActor();

        long      start   = System.currentTimeMillis();
        Throwable failure = null;

        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            long         durationMs = System.currentTimeMillis() - start;
            AuditOutcome outcome    = (failure == null) ? AuditOutcome.SUCCESS : AuditOutcome.FAILURE;
            String       errorMsg   = (failure != null) ? failure.getMessage() : null;
            String       resourceId = resolveResourceId(pjp, audited.resourceIdExpression());

            AuditEvent event = new AuditEvent(
                action, resource, resourceId, actor, outcome, errorMsg, Instant.now(), durationMs);
            publishSafely(event);
        }
    }

    private String safeResolveActor() {
        try {
            return actorResolver.resolve();
        } catch (Exception ex) {
            log.warn("ActorResolver failed: {}", ex.getMessage());
            return "unknown";
        }
    }

    /**
     * Resolves the resource ID by matching the bare parameter name (e.g., {@code "#orderId"})
     * against the method's parameter names at the join point.
     */
    private String resolveResourceId(ProceedingJoinPoint pjp, String expression) {
        if (expression == null || expression.isBlank()) return null;
        try {
            MethodSignature sig    = (MethodSignature) pjp.getSignature();
            String[]        names  = sig.getParameterNames();
            Object[]        args   = pjp.getArgs();
            String          target = expression.startsWith("#") ? expression.substring(1) : expression;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(target) && args[i] != null) {
                    return args[i].toString();
                }
            }
        } catch (Exception ex) {
            log.debug("Could not resolve resourceIdExpression '{}': {}", expression, ex.getMessage());
        }
        return null;
    }

    private void publishSafely(AuditEvent event) {
        try {
            sink.publish(event);
        } catch (Exception ex) {
            log.error("Failed to publish audit event for action='{}': {}",
                event.action(), ex.getMessage(), ex);
        }
    }
}
