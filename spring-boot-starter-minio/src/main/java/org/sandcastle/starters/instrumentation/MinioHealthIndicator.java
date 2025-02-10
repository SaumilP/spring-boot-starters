package org.sandcastle.starters.instrumentation;

import org.sandcastle.starters.properties.MinioConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;

@ConditionalOnClass(ManagementContextAutoConfiguration.class)
@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;
    private final MinioConfigurationProperties props;

    @Autowired
    public MinioHealthIndicator(MinioClient minioClient, MinioConfigurationProperties props) {
        this.minioClient = minioClient;
        this.props = props;
    }

    @Override
    public Health health() {
        if (minioClient == null) {
            return Health.down().build();
        }

        try {
            var args = BucketExistsArgs.builder().bucket(props.getBucket()).build();
            if (minioClient.bucketExists(args)) {
                return Health.up().withDetail("bucketName", props.getBucket()).build();
            } else {
                return Health.down().withDetail("bucketName", props.getBucket()).build();
            }
        } catch (Exception ex) {
            return Health.down(ex).withDetail("bucketName", props.getBucket()).build();
        }
    }

}