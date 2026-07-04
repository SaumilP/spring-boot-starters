# spring-boot-starter-minio

A Spring Boot starter that provides a fully configured MinIO client and `StorageService`
bean for S3-compatible object storage operations — including a health indicator and
Micrometer operation metrics — wired automatically when the MinIO SDK is on the classpath.

[![Maven Central](https://img.shields.io/maven-central/v/org.sandcastle.starter-apps/spring-boot-starter-minio.svg)](https://central.sonatype.com/artifact/org.sandcastle.starter-apps/spring-boot-starter-minio)

---

## Contents

- [Installation](#installation)
- [Configuration Properties](#configuration-properties)
- [What Gets Auto-Configured](#what-gets-auto-configured)
- [StorageService — Core Operations](#storageservice--core-operations)
- [Health Indicator](#health-indicator)
- [Micrometer Metrics](#micrometer-metrics)
- [Proxy Support](#proxy-support)
- [Overriding Defaults](#overriding-defaults)
- [Requirements](#requirements)

---

## Installation

```groovy
// Gradle
dependencies {
    implementation 'org.sandcastle.starter-apps:spring-boot-starter-minio:2.0.0'

    // Optional — enables health indicator
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Optional — enables Micrometer metrics on storage operations
    implementation 'io.micrometer:micrometer-core'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
}
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.sandcastle.starter-apps</groupId>
    <artifactId>spring-boot-starter-minio</artifactId>
    <version>2.0.0</version>
</dependency>
```

---

## Configuration Properties

All properties live under the `spring.minio` prefix.

| Property | Default | Description |
|---|---|---|
| `spring.minio.url` | — | MinIO server URL (required) |
| `spring.minio.access-key` | — | Access key / username (required) |
| `spring.minio.secret-key` | — | Secret key / password (required) |
| `spring.minio.bucket` | — | Default bucket name (required) |
| `spring.minio.check-bucket` | `true` | Check whether the bucket exists on startup |
| `spring.minio.create-bucket` | `false` | Auto-create the bucket if it does not exist |
| `spring.minio.region` | — | S3 region (optional) |
| `spring.minio.secure` | `false` | Enable HTTPS for the connection |
| `spring.minio.metric-name` | `minio.storage` | Micrometer timer metric name |
| `spring.minio.connect-timeout` | `10s` | Connection timeout |
| `spring.minio.write-timeout` | `60s` | Write timeout |
| `spring.minio.read-timeout` | `10s` | Read timeout |
| `spring.minio.expire` | `30s` | Pre-signed URL validity duration |

**Example `application.yml`:**

```yaml
spring:
  minio:
    url: http://minio.internal.example.com:9000
    access-key: ${MINIO_ACCESS_KEY}
    secret-key: ${MINIO_SECRET_KEY}
    bucket: my-app-assets
    create-bucket: true
    region: us-east-1
    metric-name: myapp.storage.ops
```

---

## What Gets Auto-Configured

| Bean | Type | Description |
|---|---|---|
| `minioClient` | `MinioClient` | Configured MinIO SDK client |
| `minioService` | `StorageService` | High-level storage operations API |
| `minioHealthIndicator` | `HealthIndicator` | Bucket-existence health check (requires actuator) |

---

## StorageService — Core Operations

`StorageService` abstracts all MinIO / S3 operations behind a clean interface. Inject it
into your service layer without coupling to the MinIO SDK directly.

```java
@Service
public class DocumentService {

    private final StorageService storageService;

    public DocumentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void uploadDocument(String name, InputStream content, String contentType)
            throws MinioException {
        Path target = Path.of("documents", name);
        storageService.upload(target, content, contentType);
    }

    public InputStream downloadDocument(String name) throws MinioException {
        return storageService.get(Path.of("documents", name));
    }

    public void deleteDocument(String name) throws Exception {
        storageService.removeObject("my-app-assets", "documents/" + name);
    }

    public String getShareableLink(String bucketName, String objectName) throws Exception {
        return storageService.getResignedObjectUrl(bucketName, objectName);
    }
}
```

Key methods on `StorageService`:

| Method | Description |
|---|---|
| `bucketExists(bucket)` | Returns `true` if the bucket exists |
| `createBucket(bucket)` | Creates a new bucket |
| `listBuckets()` | Returns all bucket metadata objects |
| `litBucketNames()` | Returns all bucket names as strings |
| `listObjectNames(bucket)` | Lists all object keys in a bucket |
| `upload(path, stream, contentType)` | Upload an object with content type |
| `upload(path, file)` | Upload from a `java.io.File` |
| `get(path)` | Download an object as `InputStream` |
| `getMetadata(path)` | Retrieve object stat / metadata |
| `remove(path)` | Delete an object |
| `removeObject(bucket, key)` | Delete by bucket and key |
| `removeObjects(bucket, keys)` | Batch delete; returns list of failed keys |
| `getResignedObjectUrl(bucket, key)` | Generate a pre-signed URL |
| `copy(source, target, targetBucket)` | Server-side copy |
| `getUrl(path)` | Public object URL (unsigned) |

---

## Health Indicator

When `spring-boot-starter-actuator` is on the classpath, the starter registers a health
indicator that verifies the configured default bucket is reachable.

```json
{
  "status": "UP",
  "components": {
    "minio": {
      "status": "UP",
      "details": {
        "component": "minio",
        "bucket": "my-app-assets"
      }
    }
  }
}
```

---

## Micrometer Metrics

When `micrometer-core` and `spring-aop` are on the classpath, storage operations are
timed automatically. The default metric name is `minio.storage`.

Sample Prometheus output:

```
minio_storage_seconds_count{operation="upload",status="success"} 47.0
minio_storage_seconds_count{operation="get",status="success"}    102.0
minio_storage_seconds_count{operation="remove",status="error"}    3.0
```

---

## Proxy Support

The MinIO client respects the standard JVM proxy system properties:

```bash
-Dhttp.proxyHost=proxy.internal.example.com
-Dhttp.proxyPort=3128
```

When both `http.proxyHost` and `http.proxyPort` are set, the starter automatically
configures an OkHttp proxy for the MinIO client.

---

## Overriding Defaults

Replace the `StorageService` bean to use your own implementation:

```java
@Configuration
public class StorageConfig {

    @Bean
    public StorageService storageService(MinioClient minioClient,
                                         MinioConfigurationProperties props) {
        return new MyCustomStorageServiceImpl(minioClient, props);
    }
}
```

---

## Requirements

- Java 21 or later
- Spring Boot 4.0.4 or later
- MinIO server (or any S3-compatible endpoint, e.g., AWS S3, LocalStack)

For local development, use Docker:

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"
```

Then configure:

```yaml
spring:
  minio:
    url: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: local-dev
    create-bucket: true
```
