/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.saumilp.starters.webhooks.config.WebhooksProperties;
import io.github.saumilp.starters.webhooks.model.DeliveryResult;
import io.github.saumilp.starters.webhooks.model.WebhookEndpoint;
import io.github.saumilp.starters.webhooks.model.WebhookEvent;
import io.github.saumilp.starters.webhooks.registry.InMemoryWebhookRegistry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class WebhookDeliveryServiceTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger requestCount = new AtomicInteger();
    private final InMemoryDeadLetterStore deadLetters = new InMemoryDeadLetterStore();
    private final InMemoryWebhookRegistry registry = new InMemoryWebhookRegistry();

    private WebhookDeliveryService service;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();

        WebhooksProperties props = new WebhooksProperties();
        props.getRetry().setMaxAttempts(3);
        props.getRetry().setBackoff(Duration.ofMillis(1));
        props.getRetry().setMultiplier(1.0);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(500);
        factory.setReadTimeout(500);
        RestClient client = RestClient.builder().requestFactory(factory).build();

        service = new WebhookDeliveryService(
            client, new WebhookSigner("HmacSHA256"), props, deadLetters, registry);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private void respondWith(int failuresBeforeSuccess) {
        server.createContext("/hook", exchange -> {
            int n = requestCount.incrementAndGet();
            int status = n <= failuresBeforeSuccess ? 503 : 200;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
    }

    private void respondAlways(int status) {
        server.createContext("/hook", exchange -> {
            requestCount.incrementAndGet();
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
    }

    @Test
    void should_succeedAfterRetries_when_transientFailures() {
        respondWith(2);
        var endpoint = WebhookEndpoint.active("e1", baseUrl + "/hook", "secret");

        DeliveryResult result = service.deliver(endpoint, WebhookEvent.of("order.created", "{}"));

        assertThat(result.delivered()).isTrue();
        assertThat(result.attempts()).isEqualTo(3);
        assertThat(requestCount.get()).isEqualTo(3);
        assertThat(deadLetters.all()).isEmpty();
    }

    @Test
    void should_deadLetter_when_allAttemptsFail() {
        respondAlways(500);
        var endpoint = WebhookEndpoint.active("e1", baseUrl + "/hook", "secret");

        DeliveryResult result = service.deliver(endpoint, WebhookEvent.of("order.created", "{}"));

        assertThat(result.delivered()).isFalse();
        assertThat(result.attempts()).isEqualTo(3);
        assertThat(deadLetters.all()).hasSize(1);
        assertThat(deadLetters.all().getFirst().endpointId()).isEqualTo("e1");
    }

    @Test
    void should_notRetry_when_clientError() {
        respondAlways(400);
        var endpoint = WebhookEndpoint.active("e1", baseUrl + "/hook", "secret");

        DeliveryResult result = service.deliver(endpoint, WebhookEvent.of("order.created", "{}"));

        assertThat(result.delivered()).isFalse();
        assertThat(requestCount.get()).isEqualTo(1);
        assertThat(deadLetters.all()).hasSize(1);
    }
}
