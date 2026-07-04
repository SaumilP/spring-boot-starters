# minio-example

Demonstrates the `spring-boot-starter-minio` starter with file upload, download, list, and delete operations.

## Prerequisites

- Java 21
- Docker and Docker Compose

## Running locally

**1. Start MinIO:**
```bash
docker compose up -d
```

**2. Create the demo bucket** (MinIO Console at http://localhost:9001, login: minioadmin/minioadmin):
- Or use the MinIO CLI: `mc alias set local http://localhost:9000 minioadmin minioadmin && mc mb local/demo-bucket`

**3. Run the application:**
```bash
./gradlew :examples:minio-example:bootRun
```

## API

### Upload a file
```bash
curl -X POST http://localhost:8080/files \
  -F "file=@/path/to/your/file.txt"
# {"key":"file.txt","status":"uploaded"}
```

### List files
```bash
curl http://localhost:8080/files
# ["file.txt","another.pdf"]
```

### Download a file
```bash
curl -O -J http://localhost:8080/files/file.txt
```

### Delete a file
```bash
curl -X DELETE http://localhost:8080/files/file.txt
# {"key":"file.txt","status":"deleted"}
```

## MinIO Console

Browse buckets and objects visually at **http://localhost:9001** (minioadmin / minioadmin).
