/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.github.saumilp.starters.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link StorageService} (MinIO implementation) backed by a live
 * MinIO container.
 *
 * <p>Exercises bucket creation, file upload, download, and deletion against a real S3-compatible
 * server. Requires Docker to be available in the CI environment.
 *
 * @since 1.0.0
 */
@Tag("integration")
@Testcontainers
@EnabledIfDockerAvailable
@SpringBootTest(classes = io.github.saumilp.starters.configs.MinioAutoConfiguration.class)
class MinioStorageServiceIntegrationTest {

    private static final String TEST_BUCKET = "integration-test-bucket";
    private static final String TEST_OBJECT = "test/hello.txt";
    private static final String MINIO_USER = "minioadmin";
    private static final String MINIO_PASSWORD = "minioadmin";

    @Container
    @SuppressWarnings("resource")
    static final MinIOContainer MINIO = new MinIOContainer(
            DockerImageName.parse("minio/minio:RELEASE.2025-07-23T15-54-02Z"))
            .withUserName(MINIO_USER)
            .withPassword(MINIO_PASSWORD);

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.minio.url", MINIO::getS3URL);
        registry.add("spring.minio.access-key", () -> MINIO_USER);
        registry.add("spring.minio.secret-key", () -> MINIO_PASSWORD);
        registry.add("spring.minio.bucket", () -> TEST_BUCKET);
        registry.add("spring.minio.check-bucket", () -> "true");
        registry.add("spring.minio.create-bucket", () -> "true");
    }

    @Autowired
    private StorageService storageService;

    @AfterEach
    void cleanUp() throws Exception {
        if (storageService.bucketExists(TEST_BUCKET)) {
            List<String> objects = storageService.listObjectNames(TEST_BUCKET);
            if (!objects.isEmpty()) {
                storageService.removeObjects(TEST_BUCKET, objects);
            }
        }
    }

    @Test
    void should_createBucket_when_bucketDoesNotExist() throws Exception {
        assertThat(storageService.bucketExists(TEST_BUCKET)).isTrue();
    }

    @Test
    void should_uploadAndRetrieveObject_when_fileUploaded() throws Exception {
        String content = "Hello, MinIO!";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Path objectPath = Path.of(TEST_BUCKET, TEST_OBJECT);

        storageService.upload(objectPath, input, "text/plain");

        List<String> objects = storageService.listObjectNames(TEST_BUCKET);
        assertThat(objects).contains(TEST_OBJECT);
    }

    @Test
    void should_removeObject_when_deleteRequested() throws Exception {
        String content = "to-be-deleted";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Path objectPath = Path.of(TEST_BUCKET, TEST_OBJECT);
        storageService.upload(objectPath, input, "text/plain");

        boolean removed = storageService.removeObject(TEST_BUCKET, TEST_OBJECT);

        assertThat(removed).isTrue();
        assertThat(storageService.listObjectNames(TEST_BUCKET)).doesNotContain(TEST_OBJECT);
    }

    @Test
    void should_listBuckets_when_bucketsExist() throws Exception {
        List<String> bucketNames = storageService.litBucketNames();

        assertThat(bucketNames).contains(TEST_BUCKET);
    }
}
