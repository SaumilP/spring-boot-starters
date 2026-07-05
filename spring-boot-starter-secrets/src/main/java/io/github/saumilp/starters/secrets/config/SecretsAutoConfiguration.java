/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.config;

import io.github.saumilp.starters.secrets.source.AwsSecretsManagerSecretSource;
import io.github.saumilp.starters.secrets.source.EnvironmentSecretSource;
import io.github.saumilp.starters.secrets.source.SecretSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

/**
 * Auto-configuration for the secrets starter.
 *
 * <p>Registers a single {@link SecretSource} chosen by {@code spring.secrets.provider}: an
 * {@link EnvironmentSecretSource} by default, or an {@link AwsSecretsManagerSecretSource} (plus a
 * {@link SecretsManagerClient}) when {@code provider=aws} and the AWS SDK is present. Both are
 * {@link ConditionalOnMissingBean} so a consumer can supply their own {@link SecretSource}.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(SecretsProperties.class)
@ConditionalOnProperty(prefix = "spring.secrets", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class SecretsAutoConfiguration {

    /** Creates the secrets auto-configuration. */
    public SecretsAutoConfiguration() {
    }

    /**
     * Registers the environment-backed secret source (the default provider).
     *
     * @param environment the Spring environment; must not be {@code null}
     * @return an {@link EnvironmentSecretSource}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(SecretSource.class)
    @ConditionalOnProperty(prefix = "spring.secrets", name = "provider",
        havingValue = "env", matchIfMissing = true)
    public SecretSource environmentSecretSource(Environment environment) {
        return new EnvironmentSecretSource(environment);
    }

    /**
     * AWS Secrets Manager beans, activated when {@code provider=aws} and the AWS SDK is present.
     *
     * @since 1.0.0
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SecretsManagerClient.class)
    @ConditionalOnProperty(prefix = "spring.secrets", name = "provider", havingValue = "aws")
    static class AwsSecretsConfiguration {

        /** Creates the AWS secrets configuration. */
        AwsSecretsConfiguration() {
        }

        /**
         * Builds the AWS Secrets Manager client.
         *
         * @param properties the secrets configuration; must not be {@code null}
         * @return a {@link SecretsManagerClient}; never {@code null}
         */
        @Bean
        @ConditionalOnMissingBean
        SecretsManagerClient secretsManagerClient(SecretsProperties properties) {
            var builder = SecretsManagerClient.builder()
                .region(Region.of(properties.getAws().getRegion()));
            if (StringUtils.hasText(properties.getAws().getEndpointOverride())) {
                builder.endpointOverride(URI.create(properties.getAws().getEndpointOverride()));
            }
            return builder.build();
        }

        /**
         * Registers the AWS-backed secret source.
         *
         * @param client the Secrets Manager client; must not be {@code null}
         * @return an {@link AwsSecretsManagerSecretSource}; never {@code null}
         */
        @Bean
        @ConditionalOnMissingBean(SecretSource.class)
        SecretSource awsSecretsManagerSecretSource(SecretsManagerClient client) {
            return new AwsSecretsManagerSecretSource(client);
        }
    }
}
