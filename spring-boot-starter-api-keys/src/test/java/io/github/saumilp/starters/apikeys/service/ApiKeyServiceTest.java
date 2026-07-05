/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.apikeys.hash.ApiKeyHasher;
import io.github.saumilp.starters.apikeys.model.IssuedApiKey;
import io.github.saumilp.starters.apikeys.store.InMemoryApiKeyStore;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApiKeyServiceTest {

    private final ApiKeyService service = new ApiKeyService(
        new InMemoryApiKeyStore(), new ApiKeyHasher("SHA-256"), 32, "sk");

    @Test
    void should_issueValidatableKey() {
        IssuedApiKey issued = service.issue("service-a", Set.of("read"));

        assertThat(issued.plaintext()).startsWith("sk_");
        assertThat(service.validate(issued.plaintext())).isPresent()
            .get().satisfies(k -> assertThat(k.principal()).isEqualTo("service-a"));
    }

    @Test
    void should_rejectUnknownKey() {
        assertThat(service.validate("sk_does-not-exist")).isEmpty();
        assertThat(service.validate(null)).isEmpty();
        assertThat(service.validate("  ")).isEmpty();
    }

    @Test
    void should_rejectRevokedKey() {
        IssuedApiKey issued = service.issue("service-a", Set.of());
        service.revoke(issued.apiKey().id());
        assertThat(service.validate(issued.plaintext())).isEmpty();
    }

    @Test
    void should_generateDistinctKeys() {
        assertThat(service.issue("a", Set.of()).plaintext())
            .isNotEqualTo(service.issue("a", Set.of()).plaintext());
    }
}
