/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.service;

import io.github.saumilp.starters.s3.exception.S3OperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS SDK v2 implementation of {@link S3StorageService}.
 *
 * <p>Uses the synchronous {@link S3Client} for object operations and {@link S3Presigner}
 * for generating pre-signed URLs. All AWS SDK exceptions are wrapped in
 * {@link S3OperationException} to provide a stable exception surface for callers.
 *
 * <p>This implementation is thread-safe; both {@link S3Client} and {@link S3Presigner}
 * are thread-safe by design in the AWS SDK v2.
 *
 * @since 1.0.0
 * @see S3StorageService
 */
public class S3StorageServiceImpl implements S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final S3Client s3Client;
    private final S3Presigner presigner;

    /**
     * Constructs an {@code S3StorageServiceImpl} with the given S3 client and presigner.
     *
     * @param s3Client  the AWS SDK v2 S3 client; must not be {@code null}
     * @param presigner the AWS SDK v2 S3 presigner for generating pre-signed URLs;
     *                  must not be {@code null}
     */
    public S3StorageServiceImpl(S3Client s3Client, S3Presigner presigner) {
        this.s3Client  = s3Client;
        this.presigner = presigner;
    }

    /**
     * {@inheritDoc}
     *
     * @throws S3OperationException if the AWS SDK throws during the upload
     */
    @Override
    public String upload(String bucket, String key, InputStream content,
                         long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
            s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
            log.debug("Uploaded object s3://{}/{}", bucket, key);
            return key;
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to upload object to s3://" + bucket + "/" + key + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@code ResponseInputStream} from the AWS SDK, which implements
     * {@link InputStream}. The caller must close this stream to release the HTTP connection.
     *
     * @throws S3OperationException if the object does not exist or the SDK throws
     */
    @Override
    public InputStream download(String bucket, String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            return s3Client.getObject(request);
        } catch (NoSuchKeyException ex) {
            throw new S3OperationException(
                "Object not found: s3://" + bucket + "/" + key, ex);
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to download object from s3://" + bucket + "/" + key + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws S3OperationException if the delete fails for a reason other than the object not existing
     */
    @Override
    public boolean delete(String bucket, String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
            log.debug("Deleted object s3://{}/{}", bucket, key);
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to delete object s3://" + bucket + "/" + key + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws S3OperationException if the existence check fails
     */
    @Override
    public boolean exists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to check existence of s3://" + bucket + "/" + key + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws S3OperationException if pre-signing fails
     */
    @Override
    public String presignedGetUrl(String bucket, String key, Duration validity) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(validity)
                .getObjectRequest(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build())
                .build();
            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to generate pre-signed URL for s3://" + bucket + "/" + key + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses the S3 list-objects-v2 paginator to handle buckets with more than 1,000 objects
     * transparently.
     *
     * @throws S3OperationException if the listing fails
     */
    @Override
    public List<String> listKeys(String bucket, String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
            List<String> keys = new ArrayList<>();
            s3Client.listObjectsV2Paginator(request)
                .contents()
                .forEach(obj -> keys.add(obj.key()));
            return keys;
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to list objects in s3://" + bucket + "/" + prefix + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws S3OperationException if the copy operation fails
     */
    @Override
    public void copy(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        try {
            String copySource = sourceBucket + "/" + sourceKey;
            s3Client.copyObject(CopyObjectRequest.builder()
                .copySource(copySource)
                .destinationBucket(destBucket)
                .destinationKey(destKey)
                .build());
            log.debug("Copied s3://{}/{} → s3://{}/{}", sourceBucket, sourceKey, destBucket, destKey);
        } catch (Exception ex) {
            throw new S3OperationException(
                "Failed to copy s3://" + sourceBucket + "/" + sourceKey +
                " to s3://" + destBucket + "/" + destKey + ": " + ex.getMessage(), ex);
        }
    }
}
