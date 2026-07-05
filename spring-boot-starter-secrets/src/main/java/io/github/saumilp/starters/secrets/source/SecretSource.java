/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.source;

import java.util.Optional;

/**
 * Provider-agnostic access to secret values.
 *
 * <p>Application code depends on this interface rather than a specific secrets backend, so the
 * provider (environment, AWS Secrets Manager, ...) can be swapped via configuration alone.
 *
 * @since 1.0.0
 */
public interface SecretSource {

    /**
     * Resolves a secret by name.
     *
     * @param name the secret name/identifier; must not be {@code null}
     * @return the secret value, or {@link Optional#empty()} if not found
     */
    Optional<String> get(String name);
}
