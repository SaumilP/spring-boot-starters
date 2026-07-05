/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import org.junit.jupiter.api.Test;

class InMemoryWebhookRegistryTest {

    private final InMemoryWebhookRegistry registry = new InMemoryWebhookRegistry();

    @Test
    void should_registerAndFind() {
        registry.register(WebhookEndpoint.active("e1", "https://x.example/hook", "s"));
        assertThat(registry.find("e1")).isPresent();
    }

    @Test
    void should_listOnlyActive() {
        registry.register(WebhookEndpoint.active("e1", "https://x.example/hook", "s"));
        registry.register(new WebhookEndpoint("e2", "https://y.example/hook", "s", false));
        assertThat(registry.active()).extracting(WebhookEndpoint::id).containsExactly("e1");
    }

    @Test
    void should_unregister() {
        registry.register(WebhookEndpoint.active("e1", "https://x.example/hook", "s"));
        registry.unregister("e1");
        assertThat(registry.find("e1")).isEmpty();
    }
}
