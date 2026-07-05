/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import io.github.saumilp.starters.webhooks.config.WebhooksProperties;
import io.github.saumilp.starters.webhooks.model.DeadLetter;
import io.github.saumilp.starters.webhooks.model.DeliveryResult;
import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import io.github.saumilp.starters.webhooks.model.WebhookEvent;
import io.github.saumilp.starters.webhooks.registry.WebhookRegistry;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Delivers signed webhook events to endpoints with bounded exponential-backoff retry, dead-lettering
 * exhausted deliveries.
 *
 * <p>Each request carries the HMAC signature (in the configured header), an {@code X-Webhook-Id}, and
 * an {@code X-Webhook-Event} header. {@code 4xx} responses are treated as permanent failures and are
 * not retried; connection errors and {@code 5xx} responses are retried up to the configured attempt
 * limit. When all attempts are exhausted the event is recorded in the {@link DeadLetterStore}.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class WebhookDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryService.class);

    private final RestClient restClient;
    private final WebhookSigner signer;
    private final WebhooksProperties properties;
    private final DeadLetterStore deadLetterStore;
    private final WebhookRegistry registry;

    /**
     * Creates a delivery service.
     *
     * @param restClient      the HTTP client (timeouts pre-configured); must not be {@code null}
     * @param signer          the payload signer; must not be {@code null}
     * @param properties      the starter properties; must not be {@code null}
     * @param deadLetterStore the dead-letter store; must not be {@code null}
     * @param registry        the endpoint registry; must not be {@code null}
     */
    public WebhookDeliveryService(RestClient restClient, WebhookSigner signer,
                                  WebhooksProperties properties, DeadLetterStore deadLetterStore,
                                  WebhookRegistry registry) {
        this.restClient = restClient;
        this.signer = signer;
        this.properties = properties;
        this.deadLetterStore = deadLetterStore;
        this.registry = registry;
    }

    /**
     * Delivers an event to every active endpoint in the registry.
     *
     * @param event the event to broadcast; must not be {@code null}
     * @return one {@link DeliveryResult} per active endpoint; never {@code null}
     */
    public List<DeliveryResult> deliverToAll(WebhookEvent event) {
        return registry.active().stream().map(endpoint -> deliver(endpoint, event)).toList();
    }

    /**
     * Delivers an event to a single endpoint, retrying transient failures.
     *
     * @param endpoint the target endpoint; must not be {@code null}
     * @param event    the event to deliver; must not be {@code null}
     * @return the delivery outcome; never {@code null}
     */
    public DeliveryResult deliver(WebhookEndpoint endpoint, WebhookEvent event) {
        String signature = signer.sign(event.payload(), endpoint.secret());
        int maxAttempts = Math.max(1, properties.getRetry().getMaxAttempts());
        String lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                restClient.post()
                    .uri(endpoint.url())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(properties.getSignatureHeader(), signature)
                    .header("X-Webhook-Id", event.id())
                    .header("X-Webhook-Event", event.type())
                    .body(event.payload())
                    .retrieve()
                    .toBodilessEntity();
                return DeliveryResult.delivered(endpoint.id(), attempt);
            } catch (HttpClientErrorException ex) {
                lastError = "HTTP " + ex.getStatusCode().value();
                log.warn("Webhook to '{}' failed permanently ({}); not retrying", endpoint.id(), lastError);
                break;
            } catch (RuntimeException ex) {
                lastError = ex.getMessage();
                log.warn("Webhook to '{}' attempt {}/{} failed: {}",
                    endpoint.id(), attempt, maxAttempts, lastError);
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }

        recordDeadLetter(endpoint, event, lastError);
        return DeliveryResult.failed(endpoint.id(), maxAttempts, lastError);
    }

    private void sleepBackoff(int attempt) {
        long base = properties.getRetry().getBackoff().toMillis();
        double multiplier = properties.getRetry().getMultiplier();
        long delay = (long) (base * Math.pow(multiplier, attempt - 1));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void recordDeadLetter(WebhookEndpoint endpoint, WebhookEvent event, String reason) {
        if (properties.getDeadLetter().isEnabled()) {
            deadLetterStore.store(new DeadLetter(endpoint.id(), event.id(), event.type(),
                event.payload(), reason, Instant.now()));
        }
    }
}
