/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.securityjwt.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.security-jwt.jwk-set-uri=https://issuer.example.com/.well-known/jwks.json",
        "spring.security-jwt.public-paths=/public/**"
    })
class SecurityJwtAutoConfigurationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void should_return401_when_protectedAndNoToken() throws Exception {
        assertThat(get("/secure").statusCode()).isEqualTo(401);
    }

    @Test
    void should_return200_when_publicPath() throws Exception {
        assertThat(get("/public/ping").statusCode()).isEqualTo(200);
    }

    @Test
    void should_emitSecureHeaders_when_publicPath() throws Exception {
        HttpResponse<String> response = get("/public/ping");
        assertThat(response.headers().firstValue("X-Content-Type-Options")).hasValue("nosniff");
        assertThat(response.headers().firstValue("X-Frame-Options")).hasValue("DENY");
    }

    @SpringBootApplication
    @RestController
    static class TestApp {

        @GetMapping("/secure")
        String secure() {
            return "secure";
        }

        @GetMapping("/public/ping")
        String publicPing() {
            return "pong";
        }
    }
}
