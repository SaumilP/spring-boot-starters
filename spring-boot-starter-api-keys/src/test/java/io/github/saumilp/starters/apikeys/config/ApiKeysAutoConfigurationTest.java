/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.apikeys.hash.ApiKeyHasher;
import io.github.saumilp.starters.apikeys.service.ApiKeyService;
import io.github.saumilp.starters.apikeys.store.ApiKeyStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class ApiKeysAutoConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIG =
        AutoConfigurations.of(ApiKeysAutoConfiguration.class);

    @Test
    void should_registerCoreBeansButNoFilter_when_nonWeb() {
        new ApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context)
                .hasSingleBean(ApiKeyHasher.class)
                .hasSingleBean(ApiKeyStore.class)
                .hasSingleBean(ApiKeyService.class)
                .doesNotHaveBean("apiKeyAuthFilterRegistration"));
    }

    @Test
    void should_registerFilter_when_servletWeb() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .run(context -> assertThat(context)
                .hasSingleBean(ApiKeyService.class)
                .hasBean("apiKeyAuthFilterRegistration"));
    }

    @Test
    void should_registerNothing_when_disabled() {
        new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIG)
            .withPropertyValues("spring.api-keys.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(ApiKeyService.class)
                .doesNotHaveBean("apiKeyAuthFilterRegistration"));
    }
}
