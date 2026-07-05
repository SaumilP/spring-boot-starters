/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the data-privacy starter, bound from the
 * {@code spring.data-privacy.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   data-privacy:
 *     enabled: true
 *     encryption:
 *       key: ${FIELD_ENCRYPTION_KEY}
 *     masking:
 *       enabled: true
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.data-privacy")
public class DataPrivacyProperties {

    /** Whether the data-privacy starter is enabled. */
    private boolean enabled = true;

    /** Field-level encryption settings. */
    private final Encryption encryption = new Encryption();

    /** Value/log masking settings. */
    private final Masking masking = new Masking();

    /** Creates an instance with default values. */
    public DataPrivacyProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable the data-privacy beans
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the encryption settings} */
    public Encryption getEncryption() {
        return encryption;
    }

    /** {@return the masking settings} */
    public Masking getMasking() {
        return masking;
    }

    /**
     * Field-level encryption settings.
     *
     * @since 1.0.0
     */
    public static class Encryption {

        /**
         * Secret material from which the AES-256 key is derived. When blank the field encryptor is
         * not registered and {@code EncryptedStringConverter} will fail fast if used.
         */
        private String key = "";

        /** Creates an instance with default values. */
        public Encryption() {
        }

        /** {@return the encryption secret material} */
        public String getKey() {
            return key;
        }

        /**
         * Sets the encryption secret material.
         *
         * @param key the secret; a strong, random value is recommended
         */
        public void setKey(String key) {
            this.key = key;
        }
    }

    /**
     * Value/log masking settings.
     *
     * @since 1.0.0
     */
    public static class Masking {

        /** Whether the {@code MaskingService} bean is registered. */
        private boolean enabled = true;

        /** Creates an instance with default values. */
        public Masking() {
        }

        /** {@return whether masking is enabled} */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether masking is enabled.
         *
         * @param enabled {@code false} to skip registering the masking service
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
