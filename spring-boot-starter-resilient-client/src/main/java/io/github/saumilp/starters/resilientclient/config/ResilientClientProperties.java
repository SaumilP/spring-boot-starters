/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the resilient-client starter, bound from the
 * {@code spring.resilient-client.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   resilient-client:
 *     enabled: true
 *     connect-timeout: 2s
 *     read-timeout: 5s
 *     retry:
 *       max-attempts: 3
 *       backoff: 200ms
 *     circuit-breaker:
 *       failure-rate-threshold: 50
 *       wait-duration-in-open-state: 10s
 *       sliding-window-size: 10
 *       minimum-number-of-calls: 10
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.resilient-client")
public class ResilientClientProperties {

    /** Whether the resilient client beans are registered. */
    private boolean enabled = true;

    /** Connection establishment timeout for the resilient {@code RestClient}. */
    private Duration connectTimeout = Duration.ofSeconds(2);

    /** Response read timeout for the resilient {@code RestClient}. */
    private Duration readTimeout = Duration.ofSeconds(5);

    /** Retry settings. */
    private final RetrySettings retry = new RetrySettings();

    /** Circuit-breaker settings. */
    private final CircuitBreakerSettings circuitBreaker = new CircuitBreakerSettings();

    /** Creates an instance with default values. */
    public ResilientClientProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable the resilient client beans
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the connection establishment timeout} */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection establishment timeout.
     *
     * @param connectTimeout the connect timeout; must not be {@code null}
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** {@return the response read timeout} */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the response read timeout.
     *
     * @param readTimeout the read timeout; must not be {@code null}
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /** {@return the retry settings} */
    public RetrySettings getRetry() {
        return retry;
    }

    /** {@return the circuit-breaker settings} */
    public CircuitBreakerSettings getCircuitBreaker() {
        return circuitBreaker;
    }

    /**
     * Retry settings controlling the maximum attempts and fixed backoff between attempts.
     *
     * @since 1.0.0
     */
    public static class RetrySettings {

        /** Maximum number of attempts, including the initial call. */
        private int maxAttempts = 3;

        /** Fixed wait duration between attempts. */
        private Duration backoff = Duration.ofMillis(200);

        /** Creates an instance with default values. */
        public RetrySettings() {
        }

        /** {@return the maximum number of attempts} */
        public int getMaxAttempts() {
            return maxAttempts;
        }

        /**
         * Sets the maximum number of attempts.
         *
         * @param maxAttempts the maximum attempts; must be at least 1
         */
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        /** {@return the fixed backoff between attempts} */
        public Duration getBackoff() {
            return backoff;
        }

        /**
         * Sets the fixed backoff between attempts.
         *
         * @param backoff the backoff duration; must not be {@code null}
         */
        public void setBackoff(Duration backoff) {
            this.backoff = backoff;
        }
    }

    /**
     * Circuit-breaker settings for the count-based sliding window.
     *
     * @since 1.0.0
     */
    public static class CircuitBreakerSettings {

        /** Failure-rate percentage above which the breaker opens. */
        private float failureRateThreshold = 50f;

        /** How long the breaker stays open before allowing a probe call. */
        private Duration waitDurationInOpenState = Duration.ofSeconds(10);

        /** Number of calls recorded in the count-based sliding window. */
        private int slidingWindowSize = 10;

        /** Minimum calls before the failure rate is evaluated. */
        private int minimumNumberOfCalls = 10;

        /** Creates an instance with default values. */
        public CircuitBreakerSettings() {
        }

        /** {@return the failure-rate percentage threshold} */
        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        /**
         * Sets the failure-rate percentage threshold.
         *
         * @param failureRateThreshold the threshold percentage (0-100)
         */
        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        /** {@return how long the breaker stays open} */
        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        /**
         * Sets how long the breaker stays open before a probe call.
         *
         * @param waitDurationInOpenState the open-state duration; must not be {@code null}
         */
        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }

        /** {@return the count-based sliding window size} */
        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        /**
         * Sets the count-based sliding window size.
         *
         * @param slidingWindowSize the window size; must be at least 1
         */
        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        /** {@return the minimum number of calls before evaluating the failure rate} */
        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        /**
         * Sets the minimum number of calls before the failure rate is evaluated.
         *
         * @param minimumNumberOfCalls the minimum call count; must be at least 1
         */
        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }
    }
}
