/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CachedResponse} record construction and validation.
 */
class CachedResponseTest {

    @Test
    void should_createRecord_when_allFieldsProvided() {
        CachedResponse response = new CachedResponse(
            200, Map.of("Content-Type", "application/json"),
            "{\"id\":1}", Instant.now(), "key-abc");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"id\":1}");
        assertThat(response.idempotencyKey()).isEqualTo("key-abc");
        assertThat(response.headers()).containsEntry("Content-Type", "application/json");
        assertThat(response.cachedAt()).isNotNull();
    }

    @Test
    void should_throwException_when_headersNull() {
        assertThatThrownBy(() -> new CachedResponse(200, null, "body", Instant.now(), "key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("headers");
    }

    @Test
    void should_throwException_when_bodyNull() {
        assertThatThrownBy(() -> new CachedResponse(200, Map.of(), null, Instant.now(), "key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("body");
    }

    @Test
    void should_throwException_when_cachedAtNull() {
        assertThatThrownBy(() -> new CachedResponse(200, Map.of(), "body", null, "key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cachedAt");
    }

    @Test
    void should_throwException_when_idempotencyKeyBlank() {
        assertThatThrownBy(() -> new CachedResponse(200, Map.of(), "body", Instant.now(), "  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("idempotencyKey");
    }

    @Test
    void should_throwException_when_idempotencyKeyNull() {
        assertThatThrownBy(() -> new CachedResponse(200, Map.of(), "body", Instant.now(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("idempotencyKey");
    }
}
