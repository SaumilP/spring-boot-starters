package org.sandcastle.starters.services;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.sandcastle.starters.exceptions.MinioException;

import io.minio.Result;
import io.minio.SnowballObject;
import io.minio.StatObjectResponse;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Retention;
import io.minio.messages.SseAlgorithm;

public interface StorageService {

        boolean bucketExists(String bucketName) throws Exception;

        void createBucket(String bucketName) throws Exception;

        List<Bucket> listBuckets() throws Exception;

        List<String> litBucketNames() throws Exception;

        boolean removeBucket(String bucketName) throws Exception;

        List<String> listObjectNames(String bucketName) throws Exception;

        Iterable<Result<Item>> listObjects(String bucketName) throws Exception;

        List<String> listFiles();

        List<Item> list();

        String getUrl(String path) throws MinioException;

        List<Item> fullList();

        List<Item> list(Path path);

        List<Item> getFullList(Path path);

        InputStream get(Path path) throws MinioException;

        StatObjectResponse getMetadata(Path path) throws MinioException;

        Retention getObjectRetention(Path path) throws MinioException;

        Map<Path, StatObjectResponse> getMetadata(Iterable<Path> paths);

        void getAndSave(Path source, String fileName) throws MinioException;

        void getAndSave(Path source, String fileName, String algorithm, int keyStrength) throws MinioException;

        void upload(Path source, InputStream file, Map<String, String> headers) throws MinioException;

        void upload(Path source,
                        InputStream file,
                        Map<String, String> headers,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void upload(Path source, InputStream file) throws MinioException;

        void upload(Path source,
                        InputStream file,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void upload(Path source,
                        InputStream file,
                        String contentType,
                        Map<String, String> headers) throws MinioException;

        void upload(Path source,
                        InputStream file,
                        String contentType,
                        Map<String, String> headers,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void upload(Path source, InputStream file, String contentType) throws MinioException;

        void upload(Path source, File file) throws MinioException;

        void upload(Path source,
                        InputStream file,
                        String contentType,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void upload(Path source,
                        File file,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void uploadSnowballObjects(SnowballObject... objects) throws MinioException;

        String getResignedObjectUrl(String bucketName, String objectName) throws Exception;

        void remove(Path source) throws MinioException;

        String upload(InputStream file, Path source, String contentType) throws MinioException;

        boolean removeObject(String bucketName, String objectName) throws Exception;

        List<String> removeObjects(String bucketName, List<String> objectNames) throws Exception;

        String interceptUrl(InputStream file, Path source, String contentType) throws MinioException;

        Object copy(Path source, Path target, String targetBucketName) throws MinioException;

        Object secureCopy(Path source,
                        Path target,
                        String targetBucketName,
                        String algorithm,
                        int keyStrength) throws MinioException;

        void enableLegalHold(String objVersionId, Path source) throws MinioException;

        void disableLegalHold(Path source) throws MinioException;

        boolean isObjectLegalHold(String objVersionId, Path source) throws MinioException;

        void restoreObject(String objVersionId, Path oldSource) throws MinioException;

        SseAlgorithm getEncryptionConfig() throws MinioException;

}
