/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.config;

import io.github.saumilp.starters.observability.async.MdcTaskDecorator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservabilityAutoConfiguration} conditional wiring.
 */
class ObservabilityAutoConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIG =
        AutoConfigurations.of(ObservabilityAutoConfiguration.class);

    @Test
    void should_registerTaskDecoratorButNoFilter_when_nonWeb() {
        new ApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context)
                .hasSingleBean(MdcTaskDecorator.class)
                .doesNotHaveBean("correlationIdFilterRegistration"));
    }

    @Test
    void should_registerFilterAndDecorator_when_servletWeb() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context)
                .hasSingleBean(MdcTaskDecorator.class)
                .hasBean("correlationIdFilterRegistration"));
    }

    @Test
    void should_registerNothing_when_disabled() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .withPropertyValues("spring.observability.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(MdcTaskDecorator.class)
                .doesNotHaveBean("correlationIdFilterRegistration"));
    }
}
