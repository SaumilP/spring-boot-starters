package org.sandcastle.starters.configs;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import okhttp3.OkHttpClient;
import org.sandcastle.starters.exceptions.MinioException;
import org.sandcastle.starters.properties.MinioConfigurationProperties;
import org.sandcastle.starters.services.MinioService;
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
    public MinioService minioService(MinioClient minioClient, MinioConfigurationProperties minioConfigurationProperties){
        return new MinioService(minioClient, minioConfigurationProperties);
    }

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioConfigurationProperties clientProps = minioConfigurationProperties();
        MinioClient client;
        // Enable proxy configurations
        if (!isProxyConfigured()) {
            client = MinioClient.builder()
                    .endpoint(clientProps.getUrl())
                    .credentials(clientProps.getAccessKey(), clientProps.getSecretKey())
                    .build();
        } else {
            client = MinioClient.builder()
                    .endpoint(clientProps.getUrl())
                    .credentials(clientProps.getAccessKey(), clientProps.getSecretKey())
                    .httpClient(client())
                    .build();
        }
        client.setTimeout(
                clientProps.getConnectTimeout().toMillis(),
                clientProps.getWriteTimeout().toMillis(),
                clientProps.getReadTimeout().toMillis()
        );

        // check configured bucket
        if (clientProps.isCheckBucket()) {
            try {
                log.debug("Checking if the bucket {} exists", clientProps.getBucket());
                BucketExistsArgs existsArgs = BucketExistsArgs.builder()
                        .bucket(clientProps.getBucket())
                        .build();
                boolean doesBucketExist = client.bucketExists(existsArgs);
                if (!doesBucketExist) {
                    if (clientProps.isCreateBucket()) {
                        try {
                            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                                    .bucket(clientProps.getBucket())
                                    .build();
                            client.makeBucket(makeBucketArgs);
                        } catch (Exception ex){
                            throw new MinioException("Cannot create bucket", ex.getCause());
                        }
                    } else {
                        throw new IllegalStateException("Bucket does not exist:" + clientProps.getBucket());
                    }
                }
            } catch (Exception ex){
                log.error("Error while checking bucket", ex);
                throw ex;
            }
        }
        return client;
    }

    private boolean isProxyConfigured() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");
        return StringUtils.hasText(httpHost) && StringUtils.hasText(httpPort);
    }

    private OkHttpClient client() {
        String httpHost = System.getProperty("http.proxyHost");
        String httpPort = System.getProperty("http.proxyPort");
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if (StringUtils.hasText(httpHost)) {
            httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpHost, Integer.parseInt(httpPort))));
        }
        return httpClientBuilder.build();
    }
}
