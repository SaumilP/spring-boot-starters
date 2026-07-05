package io.github.saumilp.starters.configs;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import okhttp3.OkHttpClient;
import io.github.saumilp.starters.exceptions.MinioException;
import io.github.saumilp.starters.properties.MinioConfigurationProperties;
import io.github.saumilp.starters.services.MinioStorageServiceImpl;
import io.github.saumilp.starters.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Auto-configuration for the MinIO storage starter.
 *
 * <p>Registers the {@link MinioConfigurationProperties}, a {@link MinioClient} (with optional
 * proxy, region, and bucket bootstrapping), and the {@link StorageService} implementation.
 *
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioConfigurationProperties.class)
public class MinioAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MinioAutoConfiguration.class);

    /** Creates the MinIO auto-configuration. */
    public MinioAutoConfiguration() {
    }

    /**
     * Provides the {@link StorageService} backed by the configured MinIO client.
     *
     * @param minioClient the MinIO client; must not be {@code null}
     * @param configProps the MinIO configuration properties; must not be {@code null}
     * @return the storage service; never {@code null}
     */
    @Bean
    public StorageService minioService(MinioClient minioClient, MinioConfigurationProperties configProps) {
        return new MinioStorageServiceImpl(minioClient, configProps);
    }

    /**
     * Builds the {@link MinioClient}, applying proxy and region settings and optionally
     * verifying or creating the configured bucket on startup.
     *
     * @param clientProps the MinIO configuration properties; must not be {@code null}
     * @return a configured MinIO client; never {@code null}
     * @throws Exception if the client cannot be built or the bucket check/creation fails
     */
    @Bean
    public MinioClient minioClient(MinioConfigurationProperties clientProps) throws Exception {
        var clientBuilder = MinioClient.builder()
                .endpoint(clientProps.getUrl())
                .credentials(clientProps.getAccessKey(), clientProps.getSecretKey());

        // Enable proxy configurations
        if (isProxyConfigured()) {
            clientBuilder.httpClient(client(clientProps));
        }

        // Setup region if provided
        if (clientProps.getRegion() != null && !clientProps.getRegion().isEmpty()) {
            clientBuilder.region(clientProps.getRegion());
        }

        var client = clientBuilder.build();
        client.setTimeout(
                clientProps.getConnectTimeout().toMillis(),
                clientProps.getWriteTimeout().toMillis(),
                clientProps.getReadTimeout().toMillis());

        // check configured bucket
        if (clientProps.isCheckBucket()) {
            try {
                log.debug("Checking if the bucket {} exists", clientProps.getBucket());
                var existsArgs = BucketExistsArgs.builder().bucket(clientProps.getBucket()).build();
                if (!client.bucketExists(existsArgs)) {
                    if (clientProps.isCreateBucket()) {
                        try {
                            client.makeBucket(MakeBucketArgs.builder().bucket(clientProps.getBucket()).build());
                        } catch (Exception ex) {
                            throw new MinioException("Cannot create bucket", ex.getCause());
                        }
                    } else {
                        throw new IllegalStateException("Bucket does not exist:" + clientProps.getBucket());
                    }
                }
            } catch (Exception ex) {
                log.error("Error while checking bucket", ex);
                throw ex;
            }
        }
        return client;
    }

    private boolean isProxyConfigured() {
        var httpHost = System.getProperty("http.proxyHost");
        var httpPort = System.getProperty("http.proxyPort");
        return StringUtils.hasText(httpHost) && StringUtils.hasText(httpPort);
    }

    private OkHttpClient client(MinioConfigurationProperties props) {
        var httpHost = System.getProperty("http.proxyHost");
        var httpPort = System.getProperty("http.proxyPort");
        var okHttpClient = new OkHttpClient.Builder();
        if (StringUtils.hasText(httpHost)) {
            okHttpClient.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpHost, Integer.parseInt(httpPort))));
        }
        return okHttpClient.retryOnConnectionFailure(false).build();
    }
}
