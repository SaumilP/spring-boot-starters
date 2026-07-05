/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.webhooks;

import io.github.saumilp.starters.webhooks.delivery.WebhookDeliveryService;
import io.github.saumilp.starters.webhooks.model.DeliveryResult;
import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import io.github.saumilp.starters.webhooks.model.WebhookEvent;
import io.github.saumilp.starters.webhooks.registry.WebhookRegistry;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registers subscribers, emits events, and hosts a demo receiver so the whole loop runs locally.
 *
 * @author SaumilP
 */
@RestController
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookRegistry registry;
    private final WebhookDeliveryService delivery;

    /**
     * Creates the controller.
     *
     * @param registry the endpoint registry; must not be {@code null}
     * @param delivery the delivery service; must not be {@code null}
     */
    public WebhookController(WebhookRegistry registry, WebhookDeliveryService delivery) {
        this.registry = registry;
        this.delivery = delivery;
    }

    /**
     * Registers a subscriber endpoint.
     *
     * @param body a JSON body with {@code id}, {@code url}, {@code secret}
     * @return a confirmation map
     */
    @PostMapping("/subscribe")
    public Map<String, String> subscribe(@RequestBody Map<String, String> body) {
        registry.register(WebhookEndpoint.active(body.get("id"), body.get("url"), body.get("secret")));
        return Map.of("status", "subscribed", "id", body.get("id"));
    }

    /**
     * Broadcasts an event to every active subscriber.
     *
     * @param body a JSON body with {@code type} and {@code payload}
     * @return per-endpoint delivery results
     */
    @PostMapping("/emit")
    public List<DeliveryResult> emit(@RequestBody Map<String, String> body) {
        return delivery.deliverToAll(WebhookEvent.of(
            body.getOrDefault("type", "demo.event"),
            body.getOrDefault("payload", "{}")));
    }

    /**
     * A demo receiver endpoint you can subscribe to (points back at this app).
     *
     * @param signature the HMAC signature header
     * @param payload   the delivered body
     * @return {@code "ok"}
     */
    @PostMapping("/receive")
    public String receive(@RequestHeader("X-Webhook-Signature") String signature,
                          @RequestBody String payload) {
        log.info("Received webhook: signature={} payload={}", signature, payload);
        return "ok";
    }
}
