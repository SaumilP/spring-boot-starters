/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.registry;

import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import java.util.List;
import java.util.Optional;

/**
 * Stores the set of endpoints that outbound events are delivered to.
 *
 * <p>The default {@link InMemoryWebhookRegistry} is suitable for single-node deployments and tests.
 * Applications that persist subscriptions can supply their own bean (e.g. JPA-backed) implementing
 * this interface to override it.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public interface WebhookRegistry {

    /**
     * Adds or replaces an endpoint (keyed by {@link WebhookEndpoint#id()}).
     *
     * @param endpoint the endpoint to register; must not be {@code null}
     */
    void register(WebhookEndpoint endpoint);

    /**
     * Removes the endpoint with the given id, if present.
     *
     * @param id the endpoint id; must not be {@code null}
     */
    void unregister(String id);

    /**
     * Finds an endpoint by id.
     *
     * @param id the endpoint id; must not be {@code null}
     * @return the endpoint, if registered
     */
    Optional<WebhookEndpoint> find(String id);

    /**
     * Returns every currently active endpoint.
     *
     * @return the active endpoints; never {@code null}
     */
    List<WebhookEndpoint> active();
}
