# aws-s3-example

Demonstrates the `spring-boot-starter-aws-s3` starter with [LocalStack](https://localstack.cloud/) as a local AWS S3 emulator.

## Prerequisites

- Java 21
- Docker + Docker Compose

## Running locally

```bash
# Start LocalStack (creates example-bucket automatically)
docker compose up -d

# Run the example
./gradlew :examples:aws-s3-example:bootRun
```

## API

### Upload a file
```bash
curl -F "file=@/path/to/file.txt" http://localhost:8080/s3/upload
# {"key":"file.txt","status":"uploaded"}

# With a custom key
curl -F "file=@/path/to/file.txt" -F "key=docs/hello.txt" http://localhost:8080/s3/upload
# {"key":"docs/hello.txt","status":"uploaded"}
```

### Download a file
```bash
curl -O -J http://localhost:8080/s3/download/file.txt
```

### Generate a presigned URL
```bash
curl http://localhost:8080/s3/presign/file.txt
# {"presignedUrl":"http://localhost:4566/example-bucket/file.txt?...","key":"file.txt"}
```

### Delete a file
```bash
curl -X DELETE http://localhost:8080/s3/file.txt
# {"key":"file.txt","status":"deleted"}
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.aws.s3.region` | `us-east-1` | AWS region |
| `spring.aws.s3.access-key` | — | AWS access key (omit for IAM role) |
| `spring.aws.s3.secret-key` | — | AWS secret key |
| `spring.aws.s3.endpoint-override` | — | Override endpoint (LocalStack: `http://localhost:4566`) |
| `spring.aws.s3.path-style-access` | `false` | Enable path-style access (required for LocalStack) |
| `spring.aws.s3.default-bucket` | — | Bucket used when no bucket is specified |
| `spring.aws.s3.presigned-url-expiry-minutes` | `60` | Presigned URL TTL |
