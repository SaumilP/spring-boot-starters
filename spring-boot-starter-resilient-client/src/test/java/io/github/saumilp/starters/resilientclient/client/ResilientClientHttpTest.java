/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.client;

import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test exercising {@link ResilientClient} retries over real HTTP, using an in-process
 * JDK {@link HttpServer} (no external infrastructure or Docker required).
 */
class ResilientClientHttpTest {

    private HttpServer server;
    private int port;
    private final AtomicInteger hits = new AtomicInteger();

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/data", exchange -> {
            int attempt = hits.incrementAndGet();
            if (attempt < 3) {
                exchange.sendResponseHeaders(503, -1);
                exchange.close();
            } else {
                byte[] body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void should_retryOverHttp_untilSuccess() {
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        Retry retry = Retry.of("http", RetryConfig.custom()
            .maxAttempts(3).waitDuration(Duration.ofMillis(10)).build());
        ResilientClient client = new ResilientClient(retry, CircuitBreaker.ofDefaults("http"));

        String body = client.execute(() ->
            restClient.get().uri("/data").retrieve().body(String.class));

        assertThat(body).isEqualTo("ok");
        assertThat(hits.get()).isEqualTo(3);
    }
}
