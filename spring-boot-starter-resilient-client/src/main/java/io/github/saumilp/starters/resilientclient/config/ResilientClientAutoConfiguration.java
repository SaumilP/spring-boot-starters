/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.saumilp.starters.resilientclient.client.ResilientClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for the resilient-client starter.
 *
 * <p>Registers a Resilience4j {@link Retry} and {@link CircuitBreaker} bound to
 * {@code spring.resilient-client.*}, a {@link ResilientClient} façade combining them, and — when
 * {@link RestClient} is on the classpath — a {@code resilientRestClient} pre-configured with the
 * connect/read timeouts. Every bean is {@link ConditionalOnMissingBean}.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(Retry.class)
@EnableConfigurationProperties(ResilientClientProperties.class)
@ConditionalOnProperty(prefix = "spring.resilient-client", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ResilientClientAutoConfiguration {

    /** Creates the resilient-client auto-configuration. */
    public ResilientClientAutoConfiguration() {
    }

    /**
     * Builds the shared retry policy.
     *
     * @param properties the resilient-client configuration; must not be {@code null}
     * @return a configured {@link Retry}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public Retry resilientClientRetry(ResilientClientProperties properties) {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(properties.getRetry().getMaxAttempts())
            .waitDuration(properties.getRetry().getBackoff())
            .build();
        return Retry.of("resilient-client", config);
    }

    /**
     * Builds the shared circuit breaker.
     *
     * @param properties the resilient-client configuration; must not be {@code null}
     * @return a configured {@link CircuitBreaker}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreaker resilientClientCircuitBreaker(ResilientClientProperties properties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(properties.getCircuitBreaker().getFailureRateThreshold())
            .waitDurationInOpenState(properties.getCircuitBreaker().getWaitDurationInOpenState())
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(properties.getCircuitBreaker().getSlidingWindowSize())
            .minimumNumberOfCalls(properties.getCircuitBreaker().getMinimumNumberOfCalls())
            .build();
        return CircuitBreaker.of("resilient-client", config);
    }

    /**
     * Combines the retry and circuit breaker into a {@link ResilientClient} façade.
     *
     * @param retry          the retry policy; must not be {@code null}
     * @param circuitBreaker the circuit breaker; must not be {@code null}
     * @return a {@link ResilientClient}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public ResilientClient resilientClient(Retry retry, CircuitBreaker circuitBreaker) {
        return new ResilientClient(retry, circuitBreaker);
    }

    /**
     * Provides a {@link RestClient} pre-configured with the connect and read timeouts, activated
     * when {@link RestClient} is on the classpath.
     *
     * @since 1.0.0
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestClient.class)
    static class RestClientConfiguration {

        /** Creates the RestClient configuration. */
        RestClientConfiguration() {
        }

        /**
         * Builds the timeout-configured {@code RestClient}.
         *
         * @param properties the resilient-client configuration; must not be {@code null}
         * @return a {@link RestClient} with the configured timeouts; never {@code null}
         */
        @Bean("resilientRestClient")
        @ConditionalOnMissingBean(name = "resilientRestClient")
        RestClient resilientRestClient(ResilientClientProperties properties) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(properties.getConnectTimeout());
            factory.setReadTimeout(properties.getReadTimeout());
            return RestClient.builder().requestFactory(factory).build();
        }
    }
}
