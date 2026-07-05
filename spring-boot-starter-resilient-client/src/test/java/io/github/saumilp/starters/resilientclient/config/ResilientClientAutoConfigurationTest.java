/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.saumilp.starters.resilientclient.client.ResilientClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ResilientClientAutoConfiguration} conditional wiring.
 */
class ResilientClientAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ResilientClientAutoConfiguration.class));

    @Test
    void should_registerCoreBeansAndRestClient_when_restClientPresent() {
        runner.run(context -> assertThat(context)
            .hasSingleBean(Retry.class)
            .hasSingleBean(CircuitBreaker.class)
            .hasSingleBean(ResilientClient.class)
            .hasBean("resilientRestClient"));
    }

    @Test
    void should_registerCoreBeansButNoRestClient_when_restClientAbsent() {
        runner.withClassLoader(new FilteredClassLoader(RestClient.class))
            .run(context -> assertThat(context)
                .hasSingleBean(ResilientClient.class)
                .doesNotHaveBean("resilientRestClient"));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.resilient-client.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(ResilientClient.class)
                .doesNotHaveBean(Retry.class));
    }
}
