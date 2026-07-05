/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.actor;

/**
 * A no-op {@link ActorResolver} used when Spring Security is not on the classpath.
 *
 * <p>Always returns {@code "anonymous"}. Applications that require actor resolution without
 * Spring Security should declare their own {@link ActorResolver} bean.
 *
 * @since 1.0.0
 */
public class FallbackActorResolver implements ActorResolver {

    /** Creates a new fallback resolver. */
    public FallbackActorResolver() {
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "anonymous"} unconditionally
     */
    @Override
    public String resolve() {
        return "anonymous";
    }
}
