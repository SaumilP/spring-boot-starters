/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the api-keys starter, bound from {@code spring.api-keys.*}.
 *
 * <pre>{@code
 * spring:
 *   api-keys:
 *     enabled: true
 *     header-name: X-Api-Key
 *     prefix: sk
 *     protected-paths:
 *       - /internal/**
 * }</pre>
 *
 * @author SaumilP
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.api-keys")
public class ApiKeysProperties {

    /** Whether the api-keys wiring is enabled. */
    private boolean enabled = true;

    /** HTTP header carrying the API key. */
    private String headerName = "X-Api-Key";

    /** JCA {@code MessageDigest} algorithm used to hash keys at rest. */
    private String hashAlgorithm = "SHA-256";

    /** Number of random bytes of entropy in a generated key. */
    private int keyBytes = 32;

    /** Short prefix prepended to generated keys (e.g. {@code sk}). */
    private String prefix = "sk";

    /** Ant-style paths the enforcement filter protects. Empty means the filter enforces nothing. */
    private final List<String> protectedPaths = new ArrayList<>();

    /** Creates an instance with default values. */
    public ApiKeysProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable api-key wiring
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the API key header name} */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Sets the API key header name.
     *
     * @param headerName the header name
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /** {@return the hash algorithm} */
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * Sets the hash algorithm.
     *
     * @param hashAlgorithm a JCA {@code MessageDigest} algorithm name
     */
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    /** {@return the key entropy in bytes} */
    public int getKeyBytes() {
        return keyBytes;
    }

    /**
     * Sets the key entropy in bytes.
     *
     * @param keyBytes the number of random bytes
     */
    public void setKeyBytes(int keyBytes) {
        this.keyBytes = keyBytes;
    }

    /** {@return the generated-key prefix} */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the generated-key prefix.
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /** {@return the mutable list of protected paths} */
    public List<String> getProtectedPaths() {
        return protectedPaths;
    }
}
