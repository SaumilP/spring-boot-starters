/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the secrets starter, bound from the {@code spring.secrets.*}
 * namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   secrets:
 *     enabled: true
 *     provider: aws
 *     aws:
 *       region: eu-west-1
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.secrets")
public class SecretsProperties {

    /** Whether a {@code SecretSource} is auto-configured. */
    private boolean enabled = true;

    /** The secret provider to use. */
    private Provider provider = Provider.ENV;

    /** AWS Secrets Manager settings (used when {@code provider=aws}). */
    private final Aws aws = new Aws();

    /** Creates an instance with default values. */
    public SecretsProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable secret-source auto-configuration
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the configured provider} */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Sets the provider.
     *
     * @param provider the provider; must not be {@code null}
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /** {@return the AWS settings} */
    public Aws getAws() {
        return aws;
    }

    /**
     * The available secret providers.
     *
     * @since 1.0.0
     */
    public enum Provider {

        /** Resolve secrets from the Spring {@code Environment} (default). */
        ENV,

        /** Resolve secrets from AWS Secrets Manager. */
        AWS
    }

    /**
     * AWS Secrets Manager settings.
     *
     * @since 1.0.0
     */
    public static class Aws {

        /** AWS region for the Secrets Manager client. */
        private String region = "us-east-1";

        /** Optional endpoint override (e.g. for LocalStack); blank uses the standard endpoint. */
        private String endpointOverride = "";

        /** Creates an instance with default values. */
        public Aws() {
        }

        /** {@return the AWS region} */
        public String getRegion() {
            return region;
        }

        /**
         * Sets the AWS region.
         *
         * @param region the region code; must not be {@code null}
         */
        public void setRegion(String region) {
            this.region = region;
        }

        /** {@return the endpoint override, or an empty string for the default endpoint} */
        public String getEndpointOverride() {
            return endpointOverride;
        }

        /**
         * Sets the endpoint override.
         *
         * @param endpointOverride the endpoint URL; empty string to use the AWS default
         */
        public void setEndpointOverride(String endpointOverride) {
            this.endpointOverride = endpointOverride;
        }
    }
}
