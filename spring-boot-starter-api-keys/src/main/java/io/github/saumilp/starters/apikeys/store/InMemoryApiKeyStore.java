/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.store;

import io.github.saumilp.starters.apikeys.model.ApiKey;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe, in-memory {@link ApiKeyStore} backed by a {@link ConcurrentHashMap} keyed by id.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class InMemoryApiKeyStore implements ApiKeyStore {

    private final ConcurrentMap<String, ApiKey> byId = new ConcurrentHashMap<>();

    /** Creates an empty store. */
    public InMemoryApiKeyStore() {
    }

    @Override
    public void save(ApiKey apiKey) {
        byId.put(apiKey.id(), apiKey);
    }

    @Override
    public Optional<ApiKey> findByHash(String hashedKey) {
        return byId.values().stream().filter(k -> k.hashedKey().equals(hashedKey)).findFirst();
    }

    @Override
    public Optional<ApiKey> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void revoke(String id) {
        byId.computeIfPresent(id, (key, existing) -> existing.revoked());
    }

    @Override
    public List<ApiKey> all() {
        return List.copyOf(byId.values());
    }
}
