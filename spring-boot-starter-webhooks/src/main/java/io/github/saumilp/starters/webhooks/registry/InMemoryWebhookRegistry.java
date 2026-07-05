/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.registry;

import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe, in-memory {@link WebhookRegistry} backed by a {@link ConcurrentHashMap}.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class InMemoryWebhookRegistry implements WebhookRegistry {

    private final ConcurrentMap<String, WebhookEndpoint> endpoints = new ConcurrentHashMap<>();

    /** Creates an empty registry. */
    public InMemoryWebhookRegistry() {
    }

    @Override
    public void register(WebhookEndpoint endpoint) {
        endpoints.put(endpoint.id(), endpoint);
    }

    @Override
    public void unregister(String id) {
        endpoints.remove(id);
    }

    @Override
    public Optional<WebhookEndpoint> find(String id) {
        return Optional.ofNullable(endpoints.get(id));
    }

    @Override
    public List<WebhookEndpoint> active() {
        return endpoints.values().stream().filter(WebhookEndpoint::active).toList();
    }
}
