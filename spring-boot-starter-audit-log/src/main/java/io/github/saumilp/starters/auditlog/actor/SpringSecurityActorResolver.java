/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.actor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link ActorResolver} that resolves the current actor from Spring Security's
 * {@link SecurityContextHolder}.
 *
 * <p>If no authenticated principal is present (unauthenticated request or non-web context),
 * returns {@code "anonymous"}.
 *
 * <p>This bean is registered only when {@code spring-security-core} is on the classpath.
 * When Spring Security is absent, the {@link FallbackActorResolver} is used instead.
 *
 * @since 1.0.0
 */
public class SpringSecurityActorResolver implements ActorResolver {

    /**
     * {@inheritDoc}
     *
     * @return the {@link Authentication#getName()} of the authenticated principal, or
     *         {@code "anonymous"} if no authentication is present or the user is anonymous
     */
    @Override
    public String resolve() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }
}
