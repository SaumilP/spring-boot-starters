/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.store;

import io.github.saumilp.starters.apikeys.model.ApiKey;
import java.util.List;
import java.util.Optional;

/**
 * Persists issued {@link ApiKey} metadata (never plaintext).
 *
 * <p>The default {@link InMemoryApiKeyStore} suits single-node deployments and tests. Provide a
 * JPA- or Redis-backed bean implementing this interface to persist keys across restarts and nodes.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public interface ApiKeyStore {

    /**
     * Saves or replaces a key (keyed by {@link ApiKey#id()}).
     *
     * @param apiKey the key metadata; must not be {@code null}
     */
    void save(ApiKey apiKey);

    /**
     * Finds a key by its stored hash.
     *
     * @param hashedKey the hex hash; must not be {@code null}
     * @return the key, if present
     */
    Optional<ApiKey> findByHash(String hashedKey);

    /**
     * Finds a key by id.
     *
     * @param id the key id; must not be {@code null}
     * @return the key, if present
     */
    Optional<ApiKey> findById(String id);

    /**
     * Marks the key with the given id revoked (inactive), if present.
     *
     * @param id the key id; must not be {@code null}
     */
    void revoke(String id);

    /**
     * Returns all stored keys.
     *
     * @return every key; never {@code null}
     */
    List<ApiKey> all();
}
