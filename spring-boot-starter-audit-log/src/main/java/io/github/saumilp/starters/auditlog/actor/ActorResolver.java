/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.actor;

/**
 * Strategy interface for resolving the actor (principal identity) of the current request.
 *
 * <p>The default implementation delegates to Spring Security's {@code SecurityContextHolder}
 * when available, falling back to {@code "anonymous"} for unauthenticated requests. Consuming
 * applications can replace it by declaring their own {@code ActorResolver} bean — for example,
 * to extract a user ID from a JWT claim or a custom thread-local.
 *
 * @since 1.0.0
 */
public interface ActorResolver {

    /**
     * Returns the identity of the actor for the current execution context.
     *
     * <p>Implementations must never return {@code null}. Return {@code "anonymous"} when
     * no principal can be determined.
     *
     * @return a non-null, non-blank actor identifier
     */
    String resolve();
}
