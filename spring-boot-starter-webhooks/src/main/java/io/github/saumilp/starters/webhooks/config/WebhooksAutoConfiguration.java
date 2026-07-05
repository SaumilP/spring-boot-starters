/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.config;

import io.github.saumilp.starters.webhooks.delivery.DeadLetterStore;
import io.github.saumilp.starters.webhooks.delivery.InMemoryDeadLetterStore;
import io.github.saumilp.starters.webhooks.delivery.WebhookDeliveryService;
import io.github.saumilp.starters.webhooks.delivery.WebhookSigner;
import io.github.saumilp.starters.webhooks.registry.InMemoryWebhookRegistry;
import io.github.saumilp.starters.webhooks.registry.WebhookRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for the webhooks starter.
 *
 * <p>Registers a {@link WebhookSigner}, an {@link InMemoryWebhookRegistry}, an
 * {@link InMemoryDeadLetterStore}, a timeout-configured {@link RestClient}, and the
 * {@link WebhookDeliveryService} façade. Every bean is {@link ConditionalOnMissingBean} so any
 * component (for example a persistent registry) can be replaced.
 *
 * @author SaumilP
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(WebhooksProperties.class)
@ConditionalOnProperty(prefix = "spring.webhooks", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class WebhooksAutoConfiguration {

    /** Creates the webhooks auto-configuration. */
    public WebhooksAutoConfiguration() {
    }

    /**
     * Registers the HMAC signer.
     *
     * @param properties the starter properties; must not be {@code null}
     * @return a {@link WebhookSigner}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public WebhookSigner webhookSigner(WebhooksProperties properties) {
        return new WebhookSigner(properties.getSignatureAlgorithm());
    }

    /**
     * Registers the default in-memory endpoint registry.
     *
     * @return a {@link WebhookRegistry}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public WebhookRegistry webhookRegistry() {
        return new InMemoryWebhookRegistry();
    }

    /**
     * Registers the default in-memory dead-letter store.
     *
     * @return a {@link DeadLetterStore}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public DeadLetterStore webhookDeadLetterStore() {
        return new InMemoryDeadLetterStore();
    }

    /**
     * Builds the timeout-configured {@link RestClient} used for deliveries.
     *
     * @param properties the starter properties; must not be {@code null}
     * @return a {@link RestClient}; never {@code null}
     */
    @Bean("webhookRestClient")
    @ConditionalOnMissingBean(name = "webhookRestClient")
    public RestClient webhookRestClient(WebhooksProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        return RestClient.builder().requestFactory(factory).build();
    }

    /**
     * Registers the {@link WebhookDeliveryService}.
     *
     * @param webhookRestClient the delivery HTTP client; must not be {@code null}
     * @param signer            the payload signer; must not be {@code null}
     * @param properties        the starter properties; must not be {@code null}
     * @param deadLetterStore   the dead-letter store; must not be {@code null}
     * @param registry          the endpoint registry; must not be {@code null}
     * @return a {@link WebhookDeliveryService}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public WebhookDeliveryService webhookDeliveryService(RestClient webhookRestClient,
                                                         WebhookSigner signer,
                                                         WebhooksProperties properties,
                                                         DeadLetterStore deadLetterStore,
                                                         WebhookRegistry registry) {
        return new WebhookDeliveryService(webhookRestClient, signer, properties, deadLetterStore, registry);
    }
}
