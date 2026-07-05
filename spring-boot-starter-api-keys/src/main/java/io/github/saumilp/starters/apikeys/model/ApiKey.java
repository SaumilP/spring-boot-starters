/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.model;

import java.time.Instant;
import java.util.Set;

/**
 * Persisted metadata for an issued API key.
 *
 * <p>Only the <em>hash</em> of the key is stored — never the plaintext. The plaintext is returned
 * exactly once from {@link io.github.saumilp.starters.apikeys.service.ApiKeyService#issue} and cannot
 * be recovered afterwards.
 *
 * @param id        a stable key identifier; never {@code null}
 * @param hashedKey the hex-encoded hash of the plaintext key; never {@code null}
 * @param principal the owner this key authenticates as; never {@code null}
 * @param scopes    the scopes granted to the key; never {@code null}
 * @param active    whether the key is currently valid
 * @param createdAt when the key was issued; never {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record ApiKey(
        String id,
        String hashedKey,
        String principal,
        Set<String> scopes,
        boolean active,
        Instant createdAt) {

    /**
     * Canonical constructor validating required fields and defending {@code scopes}.
     *
     * @param id        the key identifier; must not be {@code null}
     * @param hashedKey the hashed key; must not be {@code null}
     * @param principal the owning principal; must not be {@code null}
     * @param scopes    the granted scopes; {@code null} is normalised to an empty set
     * @param active    whether the key is active
     * @param createdAt the issue timestamp; must not be {@code null}
     */
    public ApiKey {
        if (id == null || hashedKey == null || principal == null || createdAt == null) {
            throw new IllegalArgumentException("id, hashedKey, principal, and createdAt are required");
        }
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }

    /**
     * Returns a revoked copy of this key.
     *
     * @return an inactive copy; never {@code null}
     */
    public ApiKey revoked() {
        return new ApiKey(id, hashedKey, principal, scopes, false, createdAt);
    }
}
