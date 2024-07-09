package org.sandcastle.starters.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;
import java.util.StringJoiner;

@ConfigurationProperties(MinioConfigurationProperties.PREFIX)
@EnableConfigurationProperties(MinioConfigurationProperties.class)
public class MinioConfigurationProperties {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public boolean isCheckBucket() {
        return checkBucket;
    }

    public void setCheckBucket(boolean checkBucket) {
        this.checkBucket = checkBucket;
    }

    public boolean isCreateBucket() {
        return createBucket;
    }

    public void setCreateBucket(boolean createBucket) {
        this.createBucket = createBucket;
    }

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
                .toString();
    }
}
