/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.webhooks.delivery.DeadLetterStore;
import io.github.saumilp.starters.webhooks.delivery.WebhookDeliveryService;
import io.github.saumilp.starters.webhooks.delivery.WebhookSigner;
import io.github.saumilp.starters.webhooks.registry.WebhookRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class WebhooksAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(WebhooksAutoConfiguration.class));

    @Test
    void should_registerAllBeans_when_default() {
        runner.run(context -> assertThat(context)
            .hasSingleBean(WebhookSigner.class)
            .hasSingleBean(WebhookRegistry.class)
            .hasSingleBean(DeadLetterStore.class)
            .hasSingleBean(WebhookDeliveryService.class));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.webhooks.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(WebhookDeliveryService.class));
    }
}
