/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.problem.config;

import io.github.saumilp.starters.problem.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ProblemDetailsAutoConfiguration} conditional wiring.
 */
class ProblemDetailsAutoConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIG =
        AutoConfigurations.of(ProblemDetailsAutoConfiguration.class);

    @Test
    void should_registerHandler_when_servletWeb() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context).hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void should_notRegisterHandler_when_nonWeb() {
        new ApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class));
    }

    @Test
    void should_notRegisterHandler_when_disabled() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .withPropertyValues("spring.problem-details.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class));
    }
}
