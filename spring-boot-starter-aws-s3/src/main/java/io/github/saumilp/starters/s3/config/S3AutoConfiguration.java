/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.config;

import io.github.saumilp.starters.s3.health.S3HealthIndicator;
import io.github.saumilp.starters.s3.service.S3StorageService;
import io.github.saumilp.starters.s3.service.S3StorageServiceImpl;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Spring Boot auto-configuration for the AWS S3 starter.
 *
 * <p>Registers the following beans when active:
 * <ul>
 *   <li>{@link S3Client} — configured with region, optional static credentials, and optional
 *       endpoint override for LocalStack/MinIO compatibility</li>
 *   <li>{@link S3Presigner} — for generating pre-signed GET URLs</li>
 *   <li>{@link S3StorageService} — the primary storage abstraction backed by the S3 client</li>
 *   <li>{@link S3HealthIndicator} — Actuator health check probing S3 connectivity</li>
 * </ul>
 *
 * <p>All beans are annotated with {@link ConditionalOnMissingBean} so consuming applications
 * can override any individual component without disabling the rest.
 *
 * <p>The entire configuration can be disabled via:
 * <pre>{@code spring.aws.s3.enabled=false}</pre>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(S3ConfigurationProperties.class)
@ConditionalOnProperty(prefix = "spring.aws.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
public class S3AutoConfiguration {

    /** Creates the S3 auto-configuration. */
    public S3AutoConfiguration() {
    }

    /**
     * Registers the AWS SDK v2 {@link S3Client}.
     *
     * <p>When {@link S3ConfigurationProperties#getAccessKeyId()} is non-blank, static
     * credentials are used. Otherwise, the AWS default credential provider chain is used
     * (environment variables → system properties → instance profile → etc.).
     *
     * <p>When {@link S3ConfigurationProperties#getEndpointOverride()} is non-blank, the client
     * uses that endpoint instead of the standard AWS S3 endpoint, enabling LocalStack or
     * MinIO compatibility.
     *
     * @param props the starter configuration; must not be {@code null}
     * @return a configured {@link S3Client}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(S3ConfigurationProperties props) {
        S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(props.getRegion()))
            .forcePathStyle(props.isPathStyleAccess());

        if (!props.getAccessKeyId().isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())));
        }
        if (!props.getEndpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpointOverride()));
        }
        return builder.build();
    }

    /**
     * Registers the {@link S3Presigner} for generating pre-signed GET URLs.
     *
     * <p>Configured identically to {@link #s3Client} with the same region, credentials,
     * and endpoint override.
     *
     * @param props the starter configuration; must not be {@code null}
     * @return a configured {@link S3Presigner}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(S3Presigner.class)
    public S3Presigner s3Presigner(S3ConfigurationProperties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
            .region(Region.of(props.getRegion()));

        if (!props.getAccessKeyId().isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())));
        }
        if (!props.getEndpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpointOverride()));
        }
        return builder.build();
    }

    /**
     * Registers the {@link S3StorageService} backed by the configured {@link S3Client}
     * and {@link S3Presigner}.
     *
     * @param s3Client  the S3 client; must not be {@code null}
     * @param presigner the S3 presigner; must not be {@code null}
     * @return an {@link S3StorageServiceImpl}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(S3StorageService.class)
    public S3StorageService s3StorageService(S3Client s3Client, S3Presigner presigner) {
        return new S3StorageServiceImpl(s3Client, presigner);
    }

    /**
     * Registers the {@link S3HealthIndicator} when Spring Boot Actuator is on the classpath.
     *
     * @param s3Client the S3 client used to probe health; must not be {@code null}
     * @return a configured {@link S3HealthIndicator}; never {@code null}
     */
    @Bean("s3HealthIndicator")
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnMissingBean(name = "s3HealthIndicator")
    @ConditionalOnProperty(prefix = "management.health.s3", name = "enabled",
        havingValue = "true", matchIfMissing = true)
    public S3HealthIndicator s3HealthIndicator(S3Client s3Client) {
        return new S3HealthIndicator(s3Client);
    }
}
