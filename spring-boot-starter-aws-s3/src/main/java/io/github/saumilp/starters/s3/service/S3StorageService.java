/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.service;

import io.github.saumilp.starters.s3.exception.S3OperationException;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * Abstraction over AWS S3-compatible object storage operations.
 *
 * <p>This interface provides a clean, technology-agnostic surface for common S3 operations.
 * Consuming applications should depend on this interface rather than directly on
 * {@link S3StorageServiceImpl} or the AWS SDK, enabling easy substitution between S3,
 * MinIO, or any other compatible backing store without changing application code.
 *
 * <p>All methods wrap AWS SDK exceptions in {@link S3OperationException} so that callers
 * deal with a single, stable exception type.
 *
 * @since 1.0.0
 */
public interface S3StorageService {

    /**
     * Uploads an object from an {@link InputStream} to the specified bucket.
     *
     * <p>The caller is responsible for closing the {@code content} stream after this method
     * returns. The stream is consumed in full during the upload.
     *
     * @param bucket        the target S3 bucket name; must not be {@code null} or blank
     * @param key           the object key (path) within the bucket; must not be {@code null} or blank
     * @param content       the input stream containing the object data; must not be {@code null}
     * @param contentLength the exact byte length of the content stream; must be non-negative
     * @param contentType   the MIME type of the object (e.g., {@code "image/png"}); must not be {@code null}
     * @return the S3 key of the uploaded object; equal to the {@code key} parameter
     * @throws S3OperationException if the upload fails
     */
    String upload(String bucket, String key, InputStream content, long contentLength, String contentType);

    /**
     * Downloads an object from the specified bucket as an {@link InputStream}.
     *
     * <p>The caller is responsible for closing the returned stream to release the underlying
     * HTTP connection.
     *
     * @param bucket the S3 bucket name; must not be {@code null} or blank
     * @param key    the object key to download; must not be {@code null} or blank
     * @return an open {@link InputStream} over the object content; never {@code null}
     * @throws S3OperationException if the object does not exist or the download fails
     */
    InputStream download(String bucket, String key);

    /**
     * Deletes an object from the specified bucket.
     *
     * @param bucket the S3 bucket name; must not be {@code null} or blank
     * @param key    the object key to delete; must not be {@code null} or blank
     * @return {@code true} if the object was deleted; {@code false} if it did not exist
     * @throws S3OperationException if the delete fails for a reason other than the object not existing
     */
    boolean delete(String bucket, String key);

    /**
     * Checks whether an object exists in the specified bucket.
     *
     * @param bucket the S3 bucket name; must not be {@code null} or blank
     * @param key    the object key to check; must not be {@code null} or blank
     * @return {@code true} if the object exists; {@code false} otherwise
     * @throws S3OperationException if the existence check fails due to a service or network error
     */
    boolean exists(String bucket, String key);

    /**
     * Generates a pre-signed GET URL that allows unauthenticated access to the specified
     * object for the given duration.
     *
     * <p>Pre-signed URLs are useful for sharing private objects with clients without exposing
     * AWS credentials. The URL expires after {@code validity} and cannot be extended.
     *
     * @param bucket   the S3 bucket name; must not be {@code null} or blank
     * @param key      the object key; must not be {@code null} or blank
     * @param validity how long the URL should remain valid; must be a positive duration
     * @return the pre-signed URL as a string; never {@code null}
     * @throws S3OperationException if URL generation fails
     */
    String presignedGetUrl(String bucket, String key, Duration validity);

    /**
     * Lists all object keys in the specified bucket whose key begins with the given prefix.
     *
     * <p>Pass an empty string as {@code prefix} to list all objects in the bucket.
     * Results are returned in UTF-8 binary order as provided by S3.
     *
     * @param bucket the S3 bucket name; must not be {@code null} or blank
     * @param prefix the key prefix filter; must not be {@code null} (use empty string for no filter)
     * @return an ordered list of matching object keys; never {@code null}, may be empty
     * @throws S3OperationException if the listing fails
     */
    List<String> listKeys(String bucket, String prefix);

    /**
     * Copies an object from a source location to a destination within the same or a different bucket.
     *
     * <p>The source object is not modified or deleted. To move an object, copy it and then delete
     * the source.
     *
     * @param sourceBucket the source bucket name; must not be {@code null} or blank
     * @param sourceKey    the source object key; must not be {@code null} or blank
     * @param destBucket   the destination bucket name; must not be {@code null} or blank
     * @param destKey      the destination object key; must not be {@code null} or blank
     * @throws S3OperationException if the copy operation fails
     */
    void copy(String sourceBucket, String sourceKey, String destBucket, String destKey);
}
