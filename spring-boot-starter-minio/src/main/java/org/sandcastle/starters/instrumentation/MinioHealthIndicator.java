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

    private final MinioClient synClient;
    private final MinioConfigurationProperties props;

    @Autowired
    public MinioHealthIndicator(MinioClient synClient, MinioConfigurationProperties props) {
        this.synClient = synClient;
        this.props = props;
    }

    @Override
    public Health health() {
        if (synClient == null) {
            return Health.down().build();
        }

        try {
            var args = BucketExistsArgs.builder()
                    .bucket(props.getBucket())
                    .build();
            if (synClient.bucketExists(args)) {
                return Health.up().withDetail("bucketName", props.getBucket()).build();
            } else {
                return Health.down().withDetail("bucketName", props.getBucket()).build();
            }
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("bucketName", props.getBucket())
                    .build();
        }
    }

}