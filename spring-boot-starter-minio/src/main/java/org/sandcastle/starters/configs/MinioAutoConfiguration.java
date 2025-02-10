package org.sandcastle.starters.configs;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import okhttp3.OkHttpClient;
import org.sandcastle.starters.exceptions.MinioException;
import org.sandcastle.starters.properties.MinioConfigurationProperties;
import org.sandcastle.starters.services.MinioStorageServiceImpl;
import org.sandcastle.starters.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@ConditionalOnClass(MinioClient.class)
public class MinioAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MinioAutoConfiguration.class);

    @Bean
    public MinioConfigurationProperties minioConfigurationProperties() {
        return new MinioConfigurationProperties();
    }

    @Bean
    public StorageService minioService(MinioClient minioClient, MinioConfigurationProperties configProps) {
        return new MinioStorageServiceImpl(minioClient, configProps);
    }

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioConfigurationProperties clientProps = minioConfigurationProperties();

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
