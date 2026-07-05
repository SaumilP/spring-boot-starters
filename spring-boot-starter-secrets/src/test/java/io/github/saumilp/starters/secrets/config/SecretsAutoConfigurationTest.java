/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.config;

import io.github.saumilp.starters.secrets.source.AwsSecretsManagerSecretSource;
import io.github.saumilp.starters.secrets.source.EnvironmentSecretSource;
import io.github.saumilp.starters.secrets.source.SecretSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link SecretsAutoConfiguration} provider selection and conditional wiring.
 */
class SecretsAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SecretsAutoConfiguration.class));

    @Test
    void should_useEnvironmentSource_byDefault() {
        runner.run(context -> assertThat(context)
            .getBean(SecretSource.class).isInstanceOf(EnvironmentSecretSource.class));
    }

    @Test
    void should_useAwsSource_when_selected() {
        runner.withPropertyValues("spring.secrets.provider=aws")
            .withBean(SecretsManagerClient.class, () -> mock(SecretsManagerClient.class))
            .run(context -> assertThat(context)
                .getBean(SecretSource.class).isInstanceOf(AwsSecretsManagerSecretSource.class));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.secrets.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SecretSource.class));
    }
}
