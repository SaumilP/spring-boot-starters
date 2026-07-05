/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the webhooks starter, bound from {@code spring.webhooks.*}.
 *
 * <pre>{@code
 * spring:
 *   webhooks:
 *     enabled: true
 *     signature-header: X-Webhook-Signature
 *     retry:
 *       max-attempts: 3
 *       backoff: 200ms
 * }</pre>
 *
 * @author SaumilP
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.webhooks")
public class WebhooksProperties {

    /** Whether the webhook delivery wiring is enabled. */
    private boolean enabled = true;

    /** Response/request header that carries the HMAC signature. */
    private String signatureHeader = "X-Webhook-Signature";

    /** JCA {@code Mac} algorithm used to sign payloads. */
    private String signatureAlgorithm = "HmacSHA256";

    /** Connection timeout for delivery requests. */
    private Duration connectTimeout = Duration.ofSeconds(2);

    /** Read timeout for delivery requests. */
    private Duration readTimeout = Duration.ofSeconds(5);

    /** Retry settings. */
    private final Retry retry = new Retry();

    /** Dead-letter settings. */
    private final DeadLetter deadLetter = new DeadLetter();

    /** Creates an instance with default values. */
    public WebhooksProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable webhook wiring
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the signature header name} */
    public String getSignatureHeader() {
        return signatureHeader;
    }

    /**
     * Sets the signature header name.
     *
     * @param signatureHeader the header name
     */
    public void setSignatureHeader(String signatureHeader) {
        this.signatureHeader = signatureHeader;
    }

    /** {@return the signature algorithm} */
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Sets the signature algorithm.
     *
     * @param signatureAlgorithm a JCA {@code Mac} algorithm name
     */
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    /** {@return the connection timeout} */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the connection timeout
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** {@return the read timeout} */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the read timeout
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /** {@return the retry settings} */
    public Retry getRetry() {
        return retry;
    }

    /** {@return the dead-letter settings} */
    public DeadLetter getDeadLetter() {
        return deadLetter;
    }

    /**
     * Retry settings for delivery attempts.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static class Retry {

        /** Maximum number of delivery attempts (including the first). */
        private int maxAttempts = 3;

        /** Base backoff between attempts. */
        private Duration backoff = Duration.ofMillis(200);

        /** Exponential backoff multiplier. */
        private double multiplier = 2.0;

        /** Creates an instance with default values. */
        public Retry() {
        }

        /** {@return the maximum number of attempts} */
        public int getMaxAttempts() {
            return maxAttempts;
        }

        /**
         * Sets the maximum number of attempts.
         *
         * @param maxAttempts the maximum attempts (must be at least 1)
         */
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        /** {@return the base backoff} */
        public Duration getBackoff() {
            return backoff;
        }

        /**
         * Sets the base backoff.
         *
         * @param backoff the base backoff
         */
        public void setBackoff(Duration backoff) {
            this.backoff = backoff;
        }

        /** {@return the backoff multiplier} */
        public double getMultiplier() {
            return multiplier;
        }

        /**
         * Sets the backoff multiplier.
         *
         * @param multiplier the exponential multiplier
         */
        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    /**
     * Dead-letter settings.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static class DeadLetter {

        /** Whether exhausted deliveries are recorded in the dead-letter store. */
        private boolean enabled = true;

        /** Creates an instance with default values. */
        public DeadLetter() {
        }

        /** {@return whether dead-lettering is enabled} */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether dead-lettering is enabled.
         *
         * @param enabled {@code false} to skip recording exhausted deliveries
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
