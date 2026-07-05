/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.service;

import io.github.saumilp.starters.s3.exception.S3OperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3StorageServiceImpl} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class S3StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner presigner;

    private S3StorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new S3StorageServiceImpl(s3Client, presigner);
    }

    @Test
    void should_returnKey_when_uploadSucceeds() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        InputStream content = new ByteArrayInputStream("data".getBytes());
        String key = service.upload("my-bucket", "path/file.txt", content, 4L, "text/plain");

        assertThat(key).isEqualTo("path/file.txt");
    }

    @Test
    void should_returnTrue_when_objectExists() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenReturn(HeadObjectResponse.builder().build());

        assertThat(service.exists("my-bucket", "existing-key")).isTrue();
    }

    @Test
    void should_returnFalse_when_objectDoesNotExist() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("Not Found").build());

        assertThat(service.exists("my-bucket", "missing-key")).isFalse();
    }

    @Test
    void should_returnTrue_when_deleteSucceeds() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenReturn(DeleteObjectResponse.builder().build());

        assertThat(service.delete("my-bucket", "some-key")).isTrue();
    }

    @Test
    void should_returnFalse_when_deleteKeyNotFound() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("Not Found").build());

        assertThat(service.delete("my-bucket", "missing-key")).isFalse();
    }

    @Test
    void should_throwS3OperationException_when_uploadFails() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("network error"));

        InputStream content = new ByteArrayInputStream("data".getBytes());
        assertThatThrownBy(() -> service.upload("bucket", "key", content, 4L, "text/plain"))
            .isInstanceOf(S3OperationException.class)
            .hasMessageContaining("Failed to upload");
    }
}
