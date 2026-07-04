# spring-boot-starter-aws-s3

A production-ready Spring Boot starter for AWS S3 object storage, built on the AWS SDK for Java v2. Provides the same `S3StorageService` abstraction as the MinIO starter, so switching between cloud and local object storage requires only a dependency swap and configuration change — no application code changes.

---

## Features

- **`S3StorageService` interface** — upload, download, delete, exists, list keys, copy, and pre-signed GET URLs
- **AWS SDK v2** — non-blocking pagination, modern credential provider chain
- **Pre-signed URL generation** — configurable expiry via `S3Presigner`
- **LocalStack / MinIO compatible** — path-style access and endpoint override support
- **Health indicator** — Spring Boot Actuator integration probing S3 connectivity
- **`@ConditionalOnMissingBean`** — override any bean without disabling the starter

---

## Requirements

| Dependency         | Version   |
|--------------------|-----------|
| Spring Boot        | 4.x       |
| Java               | 21+       |
| AWS SDK for Java   | 2.26.27+  |
| AWS account or LocalStack | any |

---

## Installation

### Gradle

```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-aws-s3:1.0.0'
```

### Maven

```xml
<dependency>
  <groupId>io.github.saumilp.starters</groupId>
  <artifactId>spring-boot-starter-aws-s3</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Configuration

All properties are under the `spring.aws.s3` namespace.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `boolean` | `true` | Set `false` to disable auto-configuration entirely |
| `region` | `String` | `us-east-1` | AWS region where your bucket resides |
| `access-key-id` | `String` | `` | Static AWS access key. Leave blank to use the default credential chain |
| `secret-access-key` | `String` | `` | Static AWS secret key paired with `access-key-id` |
| `endpoint-override` | `String` | `` | Custom endpoint URL (e.g. `http://localhost:4566` for LocalStack) |
| `path-style-access` | `boolean` | `false` | Enable for LocalStack or MinIO (`http://endpoint/bucket/key`) |
| `default-bucket` | `String` | `` | Default bucket name; injected into your beans via properties |
| `presigned-url-expiry-minutes` | `long` | `60` | Pre-signed URL validity in minutes |

---

## Annotated application.yml

### AWS Production

```yaml
spring:
  aws:
    s3:
      region: eu-west-1
      access-key-id: ${AWS_ACCESS_KEY_ID}      # or leave blank for credential chain
      secret-access-key: ${AWS_SECRET_ACCESS_KEY}
      default-bucket: my-app-uploads
      presigned-url-expiry-minutes: 30
```

### LocalStack (local development)

```yaml
spring:
  aws:
    s3:
      endpoint-override: http://localhost:4566
      path-style-access: true
      region: us-east-1
      access-key-id: test
      secret-access-key: test
      default-bucket: local-bucket
```

---

## LocalStack Quick-Start

Save as `docker-compose.yml` in your project:

```yaml
version: '3.8'
services:
  localstack:
    image: localstack/localstack:3.4
    ports:
      - "4566:4566"
    environment:
      - SERVICES=s3
      - DEFAULT_REGION=us-east-1
```

Start with:

```bash
docker compose up -d
# Create a bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://local-bucket
```

---

## Usage

### Uploading a file

```java
@Service
public class DocumentService {

    private final S3StorageService storage;

    public DocumentService(S3StorageService storage) {
        this.storage = storage;
    }

    public String store(MultipartFile file) throws IOException {
        String key = "documents/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        return storage.upload(
            "my-app-uploads",
            key,
            file.getInputStream(),
            file.getSize(),
            file.getContentType()
        );
    }
}
```

### Downloading

```java
InputStream data = storage.download("my-app-uploads", "documents/report.pdf");
// Always close the stream when done
try (data) {
    // process stream
}
```

### Pre-signed URL

```java
// Generate a link valid for 15 minutes
String url = storage.presignedGetUrl("my-app-uploads", "documents/report.pdf", Duration.ofMinutes(15));
// Share url with the client — no credentials required
```

### Pre-signed URL response example

```
https://my-app-uploads.s3.eu-west-1.amazonaws.com/documents/report.pdf
  ?X-Amz-Algorithm=AWS4-HMAC-SHA256
  &X-Amz-Expires=900
  ...
```

### List keys with prefix

```java
List<String> keys = storage.listKeys("my-app-uploads", "documents/2024/");
```

---

## Health Indicator

When `spring-boot-starter-actuator` is on the classpath, the health endpoint reports:

```json
{
  "status": "UP",
  "components": {
    "s3": {
      "status": "UP",
      "details": {
        "bucketCount": 3
      }
    }
  }
}
```

Disable with:

```yaml
management:
  health:
    s3:
      enabled: false
```

---

## Overriding Beans

All beans use `@ConditionalOnMissingBean`. To provide a custom `S3Client`:

```java
@Bean
public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.EU_WEST_1)
        .credentialsProvider(InstanceProfileCredentialsProvider.create())
        .build();
}
```

To provide a completely custom storage implementation:

```java
@Bean
public S3StorageService storageService() {
    return new MyCustomS3StorageService();
}
```

---

## Supported Versions

| Component        | Version   |
|------------------|-----------|
| Spring Boot      | 4.0.4+    |
| Java             | 21        |
| AWS SDK for Java | 2.26.27   |
| LocalStack       | 3.4+      |

---

[GitHub](https://github.com/SaumilP/spring-boot-starters) · Apache License 2.0
