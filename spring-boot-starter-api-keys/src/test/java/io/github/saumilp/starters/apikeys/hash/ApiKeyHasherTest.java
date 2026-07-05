/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.hash;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiKeyHasherTest {

    private final ApiKeyHasher hasher = new ApiKeyHasher("SHA-256");

    @Test
    void should_hashDeterministically() {
        assertThat(hasher.hash("sk_abc")).isEqualTo(hasher.hash("sk_abc"));
    }

    @Test
    void should_matchCorrectPlaintext() {
        String stored = hasher.hash("sk_abc");
        assertThat(hasher.matches("sk_abc", stored)).isTrue();
        assertThat(hasher.matches("sk_xyz", stored)).isFalse();
    }

    @Test
    void should_matchKnownVector() {
        // SHA-256("hello") — a stable, independently verifiable vector.
        assertThat(hasher.hash("hello"))
            .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }
}
