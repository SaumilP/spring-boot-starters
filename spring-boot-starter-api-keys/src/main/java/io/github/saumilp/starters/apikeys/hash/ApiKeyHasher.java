/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.hash;

import io.github.saumilp.starters.common.exception.StarterConfigurationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes API keys for storage and compares candidates in constant time.
 *
 * <p>API keys are high-entropy random tokens, so a fast cryptographic digest (SHA-256 by default) is
 * appropriate — unlike user passwords, they do not require a slow KDF. Only the hash is ever stored.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class ApiKeyHasher {

    private final String algorithm;

    /**
     * Creates a hasher for the given digest algorithm.
     *
     * @param algorithm a JCA {@code MessageDigest} algorithm (e.g. {@code SHA-256}); must not be {@code null}
     */
    public ApiKeyHasher(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Hashes a plaintext key to a lowercase hex string.
     *
     * @param plaintext the raw key; must not be {@code null}
     * @return the hex-encoded digest; never {@code null}
     */
    public String hash(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] raw = digest.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (NoSuchAlgorithmException ex) {
            throw new StarterConfigurationException(
                "Unsupported API key hash algorithm: " + algorithm, ex);
        }
    }

    /**
     * Compares a plaintext candidate against a stored hash in constant time.
     *
     * @param plaintext  the candidate key; must not be {@code null}
     * @param storedHash the stored hex hash; must not be {@code null}
     * @return {@code true} if they match
     */
    public boolean matches(String plaintext, String storedHash) {
        byte[] candidate = hash(plaintext).getBytes(StandardCharsets.UTF_8);
        byte[] stored = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(candidate, stored);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
