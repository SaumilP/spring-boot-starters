package io.github.saumilp.starters.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;
import java.util.StringJoiner;

/**
 * Configuration properties for the MinIO storage starter, bound from the
 * {@value #PREFIX} namespace.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(MinioConfigurationProperties.PREFIX)
@EnableConfigurationProperties(MinioConfigurationProperties.class)
public class MinioConfigurationProperties {

    /** Configuration property prefix for all MinIO settings. */
    public static final String PREFIX = "spring.minio";

    private String url;
    private String accessKey;
    private String secretKey;
    private boolean secure = false;
    private String bucket;
    private String metricName = "minio.storage";
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration writeTimeout = Duration.ofSeconds(60);
    private Duration readTimeout = Duration.ofSeconds(10);
    private Duration expire = Duration.ofSeconds(30);
    private boolean checkBucket = true;
    private boolean createBucket = false;
    private String region;
    private int maxConns = 200;
    private long httpKeepAliveDurationMs = Duration.ofMinutes(5).toMillis();

    /** Creates an instance with default values. */
    public MinioConfigurationProperties() {
    }

    /** {@return the AWS/MinIO region, or {@code null} if not set} */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the AWS/MinIO region.
     *
     * @param region the region identifier
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /** {@return the HTTP connection keep-alive duration in milliseconds} */
    public long getHttpKeepAliveDurationMs() {
        return httpKeepAliveDurationMs;
    }

    /**
     * Sets the HTTP connection keep-alive duration in milliseconds.
     *
     * @param httpKeepAliveDurationMs the keep-alive duration in milliseconds
     */
    public void setHttpKeepAliveDurationMs(long httpKeepAliveDurationMs) {
        this.httpKeepAliveDurationMs = httpKeepAliveDurationMs;
    }

    /** {@return the MinIO server endpoint URL} */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the MinIO server endpoint URL.
     *
     * @param url the endpoint URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /** {@return the access key (username) used to authenticate} */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Sets the access key (username) used to authenticate.
     *
     * @param accessKey the access key
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /** {@return the secret key (password) used to authenticate} */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Sets the secret key (password) used to authenticate.
     *
     * @param secretKey the secret key
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /** {@return whether TLS is used for the connection} */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets whether TLS is used for the connection.
     *
     * @param secure {@code true} to use TLS
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /** {@return the default bucket name} */
    public String getBucket() {
        return bucket;
    }

    /**
     * Sets the default bucket name.
     *
     * @param bucket the bucket name
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /** {@return the Micrometer metric name prefix for storage operations} */
    public String getMetricName() {
        return metricName;
    }

    /**
     * Sets the Micrometer metric name prefix for storage operations.
     *
     * @param metricName the metric name prefix
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /** {@return the connection establishment timeout} */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection establishment timeout.
     *
     * @param connectTimeout the connect timeout
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** {@return the socket write timeout} */
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Sets the socket write timeout.
     *
     * @param writeTimeout the write timeout
     */
    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /** {@return the socket read timeout} */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the socket read timeout.
     *
     * @param readTimeout the read timeout
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /** {@return the default validity duration for generated pre-signed URLs} */
    public Duration getExpire() {
        return expire;
    }

    /**
     * Sets the default validity duration for generated pre-signed URLs.
     *
     * @param expire the pre-signed URL validity duration
     */
    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    /** {@return whether the default bucket existence is verified on startup} */
    public boolean isCheckBucket() {
        return checkBucket;
    }

    /**
     * Sets whether the default bucket existence is verified on startup.
     *
     * @param checkBucket {@code true} to verify bucket existence
     */
    public void setCheckBucket(boolean checkBucket) {
        this.checkBucket = checkBucket;
    }

    /** {@return whether the default bucket is created on startup if missing} */
    public boolean isCreateBucket() {
        return createBucket;
    }

    /**
     * Sets whether the default bucket is created on startup if missing.
     *
     * @param createBucket {@code true} to create the bucket if it does not exist
     */
    public void setCreateBucket(boolean createBucket) {
        this.createBucket = createBucket;
    }

    /** {@return the maximum number of pooled HTTP connections} */
    public int getMaxConns() {
        return maxConns;
    }

    /**
     * Sets the maximum number of pooled HTTP connections.
     *
     * @param maxConns the maximum connection count
     */
    public void setMaxConns(int maxConns) {
        this.maxConns = maxConns;
    }

    /** {@return a string representation of these properties} */
    @Override
    public String toString() {
        return new StringJoiner(", ", MinioConfigurationProperties.class.getSimpleName() + "[", "]")
                .add("url='" + url + "'")
                .add("accessKey='" + accessKey + "'")
                .add("secretKey='" + secretKey + "'")
                .add("secure=" + secure)
                .add("bucket='" + bucket + "'")
                .add("metricName='" + metricName + "'")
                .add("connectTimeout=" + connectTimeout)
                .add("writeTimeout=" + writeTimeout)
                .add("readTimeout=" + readTimeout)
                .add("expire=" + expire)
                .add("checkBucket=" + checkBucket)
                .add("createBucket=" + createBucket)
                .add("maxConns=" + maxConns)
                .toString();
    }
}
