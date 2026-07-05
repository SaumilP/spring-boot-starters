/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.service;

import io.github.saumilp.starters.apikeys.hash.ApiKeyHasher;
import io.github.saumilp.starters.apikeys.model.ApiKey;
import io.github.saumilp.starters.apikeys.model.IssuedApiKey;
import io.github.saumilp.starters.apikeys.store.ApiKeyStore;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Issues, validates, and revokes API keys.
 *
 * <p>Issued keys are random {@code prefix_&lt;base64url&gt;} tokens. The plaintext is returned once
 * from {@link #issue} and never stored — only its hash is persisted, so a leaked store cannot be used
 * to authenticate.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final ApiKeyStore store;
    private final ApiKeyHasher hasher;
    private final int keyBytes;
    private final String prefix;

    /**
     * Creates the service.
     *
     * @param store    the key store; must not be {@code null}
     * @param hasher   the key hasher; must not be {@code null}
     * @param keyBytes the number of random bytes in a generated key (entropy)
     * @param prefix   a short prefix prepended to generated keys (e.g. {@code sk}); must not be {@code null}
     */
    public ApiKeyService(ApiKeyStore store, ApiKeyHasher hasher, int keyBytes, String prefix) {
        this.store = store;
        this.hasher = hasher;
        this.keyBytes = keyBytes;
        this.prefix = prefix;
    }

    /**
     * Issues a new key for a principal.
     *
     * @param principal the owner the key authenticates as; must not be {@code null}
     * @param scopes    the scopes to grant; may be {@code null} (treated as empty)
     * @return the one-time plaintext plus stored metadata; never {@code null}
     */
    public IssuedApiKey issue(String principal, Set<String> scopes) {
        byte[] raw = new byte[keyBytes];
        RANDOM.nextBytes(raw);
        String plaintext = prefix + "_" + ENCODER.encodeToString(raw);
        ApiKey apiKey = new ApiKey(
            UUID.randomUUID().toString(),
            hasher.hash(plaintext),
            principal,
            scopes == null ? Set.of() : scopes,
            true,
            Instant.now());
        store.save(apiKey);
        return new IssuedApiKey(plaintext, apiKey);
    }

    /**
     * Validates a candidate key, returning its metadata when active and known.
     *
     * @param plaintext the candidate key; may be {@code null} or blank
     * @return the active key, or empty if unknown, revoked, or blank
     */
    public Optional<ApiKey> validate(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return Optional.empty();
        }
        return store.findByHash(hasher.hash(plaintext)).filter(ApiKey::active);
    }

    /**
     * Revokes the key with the given id.
     *
     * @param id the key id; must not be {@code null}
     */
    public void revoke(String id) {
        store.revoke(id);
    }
}
