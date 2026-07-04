/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

/**
 * Spring Boot Actuator {@link HealthIndicator} for AWS S3 connectivity.
 *
 * <p>Verifies S3 reachability by calling {@code listBuckets} on every health check.
 * A successful response indicates that the configured credentials and endpoint are
 * valid and the S3 service is reachable.
 *
 * <p>The response includes the number of buckets visible to the configured credentials
 * as a health detail, which is useful for diagnosing permission issues.
 *
 * <p>Health check output example:
 * <pre>{@code
 * {
 *   "status": "UP",
 *   "details": {
 *     "bucketCount": 3
 *   }
 * }
 * }</pre>
 *
 * <p>Activated only when the {@code management.health.s3.enabled} property is {@code true}
 * (the default when this starter is on the classpath).
 *
 * @since 1.0.0
 */
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    /**
     * Constructs the health indicator with the configured S3 client.
     *
     * @param s3Client the AWS SDK v2 S3 client used to probe connectivity;
     *                 must not be {@code null}
     */
    public S3HealthIndicator(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Performs the S3 connectivity check by listing buckets.
     *
     * @return {@link Health#up()} with a {@code bucketCount} detail on success;
     *         {@link Health#down()} with an {@code error} detail on failure
     */
    @Override
    public Health health() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return Health.up()
                .withDetail("bucketCount", response.buckets().size())
                .build();
        } catch (Exception ex) {
            return Health.down()
                .withDetail("error", ex.getMessage())
                .build();
        }
    }
}
