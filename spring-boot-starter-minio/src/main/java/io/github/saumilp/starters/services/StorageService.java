package io.github.saumilp.starters.services;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.github.saumilp.starters.exceptions.MinioException;

import io.minio.Result;
import io.minio.SnowballObject;
import io.minio.StatObjectResponse;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Retention;
import io.minio.messages.SseAlgorithm;

/**
 * Abstraction over MinIO/S3-compatible object storage operations.
 *
 * <p>Provides bucket management, object upload/download (with optional server-side
 * encryption), listing, pre-signed URL generation, legal-hold controls, and retention
 * inspection. Consuming applications should depend on this interface rather than the
 * concrete implementation so the backing store can be substituted without code changes.
 *
 * @since 1.0.0
 */
public interface StorageService {

    /**
     * Checks whether a bucket exists.
     *
     * @param bucketName the bucket name to check; must not be {@code null}
     * @return {@code true} if the bucket exists; {@code false} otherwise
     * @throws Exception if the underlying storage call fails
     */
    boolean bucketExists(String bucketName) throws Exception;

    /**
     * Creates a bucket if it does not already exist.
     *
     * @param bucketName the bucket name to create; must not be {@code null}
     * @throws Exception if the underlying storage call fails
     */
    void createBucket(String bucketName) throws Exception;

    /**
     * Lists all buckets visible to the configured credentials.
     *
     * @return the list of buckets; never {@code null}, may be empty
     * @throws Exception if the underlying storage call fails
     */
    List<Bucket> listBuckets() throws Exception;

    /**
     * Lists the names of all buckets visible to the configured credentials.
     *
     * @return the list of bucket names; never {@code null}, may be empty
     * @throws Exception if the underlying storage call fails
     */
    List<String> litBucketNames() throws Exception;

    /**
     * Removes a bucket. The bucket is only removed when it is empty.
     *
     * @param bucketName the bucket name to remove; must not be {@code null}
     * @return {@code true} if the bucket was removed; {@code false} if it did not exist or
     *         was not empty
     * @throws Exception if the underlying storage call fails
     */
    boolean removeBucket(String bucketName) throws Exception;

    /**
     * Lists the object keys contained in the given bucket.
     *
     * @param bucketName the bucket name; must not be {@code null}
     * @return the list of object names; never {@code null}, may be empty
     * @throws Exception if the underlying storage call fails
     */
    List<String> listObjectNames(String bucketName) throws Exception;

    /**
     * Lists the objects contained in the given bucket.
     *
     * @param bucketName the bucket name; must not be {@code null}
     * @return an iterable of object results, or {@code null} if the bucket does not exist
     * @throws Exception if the underlying storage call fails
     */
    Iterable<Result<Item>> listObjects(String bucketName) throws Exception;

    /**
     * Lists the file (object) names in the default configured bucket.
     *
     * @return the list of file names; never {@code null}, may be empty
     */
    List<String> listFiles();

    /**
     * Lists the top-level objects in the default configured bucket (non-recursive).
     *
     * @return the list of items; never {@code null}, may be empty
     */
    List<Item> list();

    /**
     * Generates a pre-signed GET URL for an object in the default configured bucket.
     *
     * @param path the object path within the bucket; must not be {@code null}
     * @return the pre-signed URL; never {@code null}
     * @throws MinioException if URL generation fails
     */
    String getUrl(String path) throws MinioException;

    /**
     * Lists all objects in the default configured bucket, recursively.
     *
     * @return the list of items; never {@code null}, may be empty
     */
    List<Item> fullList();

    /**
     * Lists the top-level objects under the given prefix in the default bucket (non-recursive).
     *
     * @param path the prefix path within the bucket; must not be {@code null}
     * @return the list of items; never {@code null}, may be empty
     */
    List<Item> list(Path path);

    /**
     * Lists all objects under the given prefix in the default bucket, recursively.
     *
     * @param path the prefix path within the bucket; must not be {@code null}
     * @return the list of items; never {@code null}, may be empty
     */
    List<Item> getFullList(Path path);

    /**
     * Opens a stream over an object in the default configured bucket.
     *
     * @param path the object path within the bucket; must not be {@code null}
     * @return an open input stream over the object content; the caller must close it
     * @throws MinioException if the object cannot be read
     */
    InputStream get(Path path) throws MinioException;

    /**
     * Retrieves the metadata (stat) for an object in the default configured bucket.
     *
     * @param path the object path within the bucket; must not be {@code null}
     * @return the object metadata; never {@code null}
     * @throws MinioException if the metadata cannot be retrieved
     */
    StatObjectResponse getMetadata(Path path) throws MinioException;

    /**
     * Retrieves the retention configuration for an object in the default bucket.
     *
     * @param path the object path within the bucket; must not be {@code null}
     * @return the object retention configuration; never {@code null}
     * @throws MinioException if the retention configuration cannot be retrieved
     */
    Retention getObjectRetention(Path path) throws MinioException;

    /**
     * Retrieves metadata for multiple objects in the default configured bucket.
     *
     * @param paths the object paths to inspect; must not be {@code null}
     * @return a map of each path to its metadata; never {@code null}
     */
    Map<Path, StatObjectResponse> getMetadata(Iterable<Path> paths);

    /**
     * Downloads an object from the default bucket and saves it to a local file.
     *
     * @param source   the object path within the bucket; must not be {@code null}
     * @param fileName the local destination file name; must not be {@code null}
     * @throws MinioException if the download fails
     */
    void getAndSave(Path source, String fileName) throws MinioException;

    /**
     * Downloads a server-side-encrypted object and saves it to a local file.
     *
     * @param source      the object path within the bucket; must not be {@code null}
     * @param fileName    the local destination file name; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the download fails
     */
    void getAndSave(Path source, String fileName, String algorithm, int keyStrength) throws MinioException;

    /**
     * Uploads an object with the given headers.
     *
     * @param source  the destination object path; must not be {@code null}
     * @param file    the object content stream; must not be {@code null}
     * @param headers the metadata headers to attach; must not be {@code null}
     * @throws MinioException if the upload fails
     */
    void upload(Path source, InputStream file, Map<String, String> headers) throws MinioException;

    /**
     * Uploads a server-side-encrypted object with the given headers.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param headers     the metadata headers to attach; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                InputStream file,
                Map<String, String> headers,
                String algorithm,
                int keyStrength) throws MinioException;

    /**
     * Uploads an object.
     *
     * @param source the destination object path; must not be {@code null}
     * @param file   the object content stream; must not be {@code null}
     * @throws MinioException if the upload fails
     */
    void upload(Path source, InputStream file) throws MinioException;

    /**
     * Uploads a server-side-encrypted object.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                InputStream file,
                String algorithm,
                int keyStrength) throws MinioException;

    /**
     * Uploads an object with an explicit content type and headers.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @param headers     the metadata headers to attach; must not be {@code null}
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                InputStream file,
                String contentType,
                Map<String, String> headers) throws MinioException;

    /**
     * Uploads a server-side-encrypted object with an explicit content type and headers.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @param headers     the metadata headers to attach; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                InputStream file,
                String contentType,
                Map<String, String> headers,
                String algorithm,
                int keyStrength) throws MinioException;

    /**
     * Uploads an object with an explicit content type.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @throws MinioException if the upload fails
     */
    void upload(Path source, InputStream file, String contentType) throws MinioException;

    /**
     * Uploads a local file as an object.
     *
     * @param source the destination object path; must not be {@code null}
     * @param file   the local file to upload; must not be {@code null}
     * @throws MinioException if the upload fails
     */
    void upload(Path source, File file) throws MinioException;

    /**
     * Uploads a server-side-encrypted object with an explicit content type.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the object content stream; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                InputStream file,
                String contentType,
                String algorithm,
                int keyStrength) throws MinioException;

    /**
     * Uploads a local file as a server-side-encrypted object.
     *
     * @param source      the destination object path; must not be {@code null}
     * @param file        the local file to upload; must not be {@code null}
     * @param algorithm   the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength the encryption key strength in bits (e.g. {@code 256})
     * @throws MinioException if the upload fails
     */
    void upload(Path source,
                File file,
                String algorithm,
                int keyStrength) throws MinioException;

    /**
     * Uploads a batch of objects using a single snowball (tar) request.
     *
     * @param objects the objects to upload; each must have a non-empty name
     * @throws MinioException if the upload fails or any object is invalid
     */
    void uploadSnowballObjects(SnowballObject... objects) throws MinioException;

    /**
     * Generates a pre-signed GET URL for an object in an arbitrary bucket.
     *
     * @param bucketName the bucket name; must not be {@code null}
     * @param objectName the object key; must not be {@code null}
     * @return the pre-signed URL; never {@code null}
     * @throws Exception if URL generation fails
     */
    String getResignedObjectUrl(String bucketName, String objectName) throws Exception;

    /**
     * Removes an object from the default configured bucket.
     *
     * @param source the object path to remove; must not be {@code null}
     * @throws MinioException if the removal fails
     */
    void remove(Path source) throws MinioException;

    /**
     * Uploads an object and returns a pre-signed URL to the stored object.
     *
     * @param file        the object content stream; must not be {@code null}
     * @param source      the destination object path; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @return a pre-signed URL to the uploaded object; never {@code null}
     * @throws MinioException if the upload fails
     */
    String upload(InputStream file, Path source, String contentType) throws MinioException;

    /**
     * Removes a single object from an arbitrary bucket.
     *
     * @param bucketName the bucket name; must not be {@code null}
     * @param objectName the object key to remove; must not be {@code null}
     * @return {@code true} if the object was removed; {@code false} if the bucket did not exist
     * @throws Exception if the removal fails
     */
    boolean removeObject(String bucketName, String objectName) throws Exception;

    /**
     * Removes multiple objects from an arbitrary bucket.
     *
     * @param bucketName  the bucket name; must not be {@code null}
     * @param objectNames the object keys to remove; must not be {@code null} or empty
     * @return the list of object names that failed to be removed; never {@code null}
     * @throws Exception if the removal fails
     */
    List<String> removeObjects(String bucketName, List<String> objectNames) throws Exception;

    /**
     * Uploads an object and returns the stored object URL with query parameters stripped.
     *
     * @param file        the object content stream; must not be {@code null}
     * @param source      the destination object path; must not be {@code null}
     * @param contentType the MIME content type; must not be {@code null}
     * @return the object URL without pre-sign query parameters; never {@code null}
     * @throws MinioException if the upload fails
     */
    String interceptUrl(InputStream file, Path source, String contentType) throws MinioException;

    /**
     * Copies an object to a target path within the given bucket.
     *
     * @param source           the source object path; must not be {@code null}
     * @param target           the destination object path; must not be {@code null}
     * @param targetBucketName the destination bucket name; must not be {@code null}
     * @return the underlying copy result object; never {@code null}
     * @throws MinioException if the copy fails
     */
    Object copy(Path source, Path target, String targetBucketName) throws MinioException;

    /**
     * Copies an object using server-side encryption on the destination.
     *
     * @param source           the source object path; must not be {@code null}
     * @param target           the destination object path; must not be {@code null}
     * @param targetBucketName the destination bucket name; must not be {@code null}
     * @param algorithm        the encryption key algorithm (e.g. {@code "AES"})
     * @param keyStrength      the encryption key strength in bits (e.g. {@code 256})
     * @return the underlying copy result object; never {@code null}
     * @throws MinioException if the copy fails
     */
    Object secureCopy(Path source,
                      Path target,
                      String targetBucketName,
                      String algorithm,
                      int keyStrength) throws MinioException;

    /**
     * Enables legal hold on a specific object version in the default bucket.
     *
     * @param objVersionId the object version identifier; must not be {@code null}
     * @param source       the object path; must not be {@code null}
     * @throws MinioException if the operation fails
     */
    void enableLegalHold(String objVersionId, Path source) throws MinioException;

    /**
     * Disables legal hold on an object in the default bucket.
     *
     * @param source the object path; must not be {@code null}
     * @throws MinioException if the operation fails
     */
    void disableLegalHold(Path source) throws MinioException;

    /**
     * Checks whether legal hold is enabled on a specific object version.
     *
     * @param objVersionId the object version identifier; must not be {@code null}
     * @param source       the object path; must not be {@code null}
     * @return {@code true} if legal hold is enabled; {@code false} otherwise
     * @throws MinioException if the check fails
     */
    boolean isObjectLegalHold(String objVersionId, Path source) throws MinioException;

    /**
     * Restores a specific object version in the default bucket.
     *
     * @param objVersionId the object version identifier; must not be {@code null}
     * @param oldSource    the object path to restore; must not be {@code null}
     * @throws MinioException if the restore fails
     */
    void restoreObject(String objVersionId, Path oldSource) throws MinioException;

    /**
     * Returns the server-side encryption algorithm configured on the default bucket.
     *
     * @return the configured SSE algorithm, or {@code null} if none is configured
     * @throws MinioException if the configuration cannot be retrieved
     */
    SseAlgorithm getEncryptionConfig() throws MinioException;

}
