/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.model;

/**
 * The result of issuing a key: the one-time plaintext plus the stored {@link ApiKey} metadata.
 *
 * <p>The {@code plaintext} must be delivered to the caller immediately and never persisted — only
 * its hash lives in the store.
 *
 * @param plaintext the raw key to hand to the client, shown only once; never {@code null}
 * @param apiKey    the stored metadata for the key; never {@code null}
 * @author SaumilP
 * @since 1.0.0
 */
public record IssuedApiKey(String plaintext, ApiKey apiKey) {
}
