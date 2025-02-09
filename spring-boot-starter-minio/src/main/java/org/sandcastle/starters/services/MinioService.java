package org.sandcastle.starters.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.java.accessibility.util.EventID;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DisableObjectLegalHoldArgs;
import io.minio.DownloadObjectArgs;
import io.minio.EnableObjectLegalHoldArgs;
import io.minio.GetBucketEncryptionArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectRetentionArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.IsObjectLegalHoldEnabledArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.RestoreObjectArgs;
import io.minio.Result;
import io.minio.ServerSideEncryption;
import io.minio.ServerSideEncryptionCustomerKey;
import io.minio.ServerSideEncryptionKms;
import io.minio.SnowballObject;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.UploadObjectArgs;
import io.minio.UploadSnowballObjectsArgs;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.RestoreRequest;
import io.minio.messages.Retention;
import io.minio.messages.SseAlgorithm;
import io.minio.messages.SseConfiguration;

import org.sandcastle.starters.exceptions.MinioException;
import org.sandcastle.starters.exceptions.MinioFetchException;
import org.sandcastle.starters.properties.MinioConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.crypto.KeyGenerator;

public class MinioService {
    private final MinioClient minioClient;
    private final MinioConfigurationProperties clientProps;

    public MinioService(MinioClient minioClient, MinioConfigurationProperties clientProps) {
        this.minioClient = minioClient;
        this.clientProps = clientProps;
    }

    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    public void createBucket(String bucketName) throws Exception {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public List<Bucket> listBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    public List<String> litBucketNames() throws Exception {
        List<Bucket> buckets = listBuckets();
        return !CollectionUtils.isEmpty(buckets)
                ? buckets.stream().map(Bucket::name).collect(Collectors.toList())
                : new ArrayList<>();
    }

    public boolean removeBucket(String bucketName) throws Exception {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                // If an object file exists, the file fails to be deleted
                if (item.size() > 0) {
                    return false;
                }
            }
            // The bucket can be deleted only when the bucket is empty.
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);
            return !flag;
        }
        return false;
    }

    public List<String> listObjectNames(String bucketName) throws Exception {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                listObjectNames.add(item.objectName());
            }
        }
        return listObjectNames;
    }

    public Iterable<Result<Item>> listObjects(String bucketName) throws Exception {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        }
        return null;
    }

    public List<String> listFiles() {
        return !CollectionUtils.isEmpty(list())
                ? list().stream()
                        .map(Item::objectName)
                        .collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
    }

    public List<Item> list() {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(clientProps.getBucket())
                .prefix("")
                .recursive(false)
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    public String getUrl(String path) throws MinioException {
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .bucket(clientProps.getBucket())
                .object(path)
                .method(Method.GET)
                .expiry(Math.toIntExact(clientProps.getExpire().getSeconds()), TimeUnit.MINUTES)
                .build();
        try {
            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (Exception e) {
            throw new MinioException("Error getting file list", e);
        }
    }

    public List<Item> fullList() {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(clientProps.getBucket())
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    public List<Item> list(Path path) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(clientProps.getBucket())
                .prefix(path.toString())
                .recursive(false)
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    public List<Item> getFullList(Path path) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(clientProps.getBucket())
                .prefix(path.toString())
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport
                .stream(myObjects.spliterator(), true)
                .map(itemResult -> {
                    try {
                        return itemResult.get();
                    } catch (Exception e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public InputStream get(Path path) throws MinioException {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.getObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public StatObjectResponse getMetadata(Path path) throws MinioException {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.statObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public Retention getObjectRetention(Path path) throws MinioException {
        try {
            GetObjectRetentionArgs args = GetObjectRetentionArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.getObjectRetention(args);
        } catch (Exception e) {
            throw new MinioException("Error while checking object retention in Minio", e);
        }
    }

    public Map<Path, StatObjectResponse> getMetadata(Iterable<Path> paths) {
        return StreamSupport.stream(paths.spliterator(), false)
                .map(path -> {
                    try {
                        StatObjectArgs args = StatObjectArgs.builder()
                                .bucket(clientProps.getBucket())
                                .object(path.toString())
                                .build();
                        return new HashMap.SimpleEntry<>(path, minioClient.statObject(args));
                    } catch (Exception e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void getAndSave(Path source, String fileName) throws MinioException {
        try {
            DownloadObjectArgs args = DownloadObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .filename(fileName)
                    .build();
            minioClient.downloadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void getAndSave(Path source, String fileName, String algorithm, int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            DownloadObjectArgs args = DownloadObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .filename(fileName)
                    .ssec(ssec)
                    .build();
            minioClient.downloadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source, InputStream file, Map<String, String> headers) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            InputStream file,
            Map<String, String> headers,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .sse(ssec)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source, InputStream file) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            InputStream file,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .sse(ssec)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            InputStream file,
            String contentType,
            Map<String, String> headers) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            InputStream file,
            String contentType,
            Map<String, String> headers,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .contentType(contentType)
                    .sse(ssec)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source, InputStream file, String contentType) throws MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source, File file) throws MinioException {
        try {
            UploadObjectArgs args = UploadObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .filename(file.getName())
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            InputStream file,
            String contentType,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .contentType(contentType)
                    .sse(ssec)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void upload(Path source,
            File file,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            UploadObjectArgs args = UploadObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .filename(file.getName())
                    .sse(ssec)
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public void uploadSnowballObjects(SnowballObject... objects) throws MinioException {
        var objIterables = new Iterable<SnowballObject>() {
            @Override
            public Iterator<SnowballObject> iterator() {
                return Arrays.stream(objects).iterator();
            }
        };
        Iterable<SnowballObject> iterable = () -> objIterables.iterator();
        if (objects == null
                || StreamSupport.stream(iterable.spliterator(), false)
                        .anyMatch(so -> so.name() == null || so.name().isEmpty())) {
            throw new MinioException("Valid input must be provided");
        }
        try {
            UploadSnowballObjectsArgs args = UploadSnowballObjectsArgs.builder()
                    .bucket(clientProps.getBucket())
                    .objects(objIterables)
                    .build();
            minioClient.uploadSnowballObjects(args);
        } catch (Exception ex) {
            throw new MinioException("Error attempting to save snowball objects", ex);
        }
    }

    public String getResignedObjectUrl(String bucketName, String objectName) throws Exception {
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(args);
    }

    public void remove(Path source) throws MinioException {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .build();
            minioClient.removeObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    public String upload(InputStream file, Path source, String contentType) throws MinioException {
        String fileUrl;
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(args);
            // result
            fileUrl = getResignedObjectUrl(clientProps.getBucket(), source.toString());
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
        return fileUrl;
    }

    public boolean removeObject(String bucketName, String objectName) throws Exception {
        if (bucketExists(bucketName)) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
            return true;
        }
        return false;
    }

    public List<String> removeObjects(String bucketName, List<String> objectNames) throws Exception {
        if (CollectionUtils.isEmpty(objectNames)) {
            throw new MinioException("minio.delete.object.name.can.not.empty");
        }

        List<String> deleteErrorNames = new ArrayList<>();
        if (bucketExists(bucketName)) {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());
            Iterable<Result<DeleteError>> results = minioClient
                    .removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                deleteErrorNames.add(error.objectName());
            }
        }
        return deleteErrorNames;
    }

    public String interceptUrl(InputStream file, Path source, String contentType) throws MinioException {
        String url;
        try {
            url = upload(file, source, contentType);
        } catch (MinioException e) {
            throw new MinioException("minio.delete.object.name.can.not.empty", e);
        }
        return url.substring(EventID.ACTION, url.indexOf("?"));
    }

    public Object copy(Path source, Path target, String targetBucketName) throws MinioException {
        try {
            var args = CopyObjectArgs.builder()
                    .bucket(targetBucketName)
                    .object(target.toString())
                    .source(
                            CopySource.builder()
                                    .bucket(clientProps.getBucket())
                                    .object(source.toString())
                                    .build())
                    .build();
            return minioClient.copyObject(args);
        } catch (Exception ex) {
            throw new MinioException("Error copying the object", ex);
        }
    }

    public Object secureCopy(Path source,
            Path target,
            String targetBucketName,
            String algorithm,
            int keyStrength) throws MinioException {
        try {
            var ssec = getServerSideEncryptionKey(algorithm, keyStrength);
            var args = CopyObjectArgs.builder()
                    .bucket(targetBucketName)
                    .object(target.toString())
                    .source(
                            CopySource.builder()
                                    .bucket(clientProps.getBucket())
                                    .object(source.toString())
                                    .build())
                    .sse(ssec)
                    .build();
            return minioClient.copyObject(args);
        } catch (Exception ex) {
            throw new MinioException("Error copying the object", ex);
        }
    }

    public void enableLegalHold(String objVersionId, Path source) throws MinioException {
        try {
            var args = EnableObjectLegalHoldArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .versionId(objVersionId)
                    .build();
            minioClient.enableObjectLegalHold(args);
        } catch (Exception ex) {
            throw new MinioException("Error enabling object legal hold", ex);
        }
    }

    public void disableLegalHold(Path source) throws MinioException {
        try {
            var args = DisableObjectLegalHoldArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .build();
            minioClient.disableObjectLegalHold(args);
        } catch (Exception ex) {
            throw new MinioException("Error disabling object legal hold", ex);
        }
    }

    public boolean isObjectLegalHold(String objVersionId, Path source) throws MinioException {
        try {
            var args = IsObjectLegalHoldEnabledArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(source.toString())
                    .versionId(objVersionId)
                    .build();
            return minioClient.isObjectLegalHoldEnabled(args);
        } catch (Exception ex) {
            throw new MinioException("Error checking object legal hold", ex);
        }
    }

    public void restoreObject(String objVersionId, Path oldSource) throws MinioException {
        try {
            var args = RestoreObjectArgs.builder()
                    .bucket(clientProps.getBucket())
                    .object(oldSource.toString())
                    .versionId(objVersionId)
                    .request(new RestoreRequest(null, null, null, objVersionId, null, null))
                    .build();
            minioClient.restoreObject(args);
        } catch (Exception ex) {
            throw new MinioException("Error checking object legal hold", ex);
        }
    }

    public SseAlgorithm getEncryptionConfig() throws MinioException {
        try {
            var args = GetBucketEncryptionArgs.builder()
                    .bucket(clientProps.getBucket())
                    .build();
            var sseConfig = minioClient.getBucketEncryption(args);
            if (sseConfig != null) {
                return sseConfig.rule().sseAlgorithm();
            }
            return null;
        } catch (Exception ex) {
            throw new MinioException("Error retrieving bucket CORS config", ex);
        }
    }

    private ServerSideEncryptionCustomerKey getServerSideEncryptionKey(String algorithm, int keyStrength)
            throws GeneralSecurityException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(keyStrength);
        ServerSideEncryptionCustomerKey ssec = new ServerSideEncryptionCustomerKey(keyGen.generateKey());
        return ssec;
    }

    private ServerSideEncryption getKmsEncrytion(Map<String, String> contextMap)
            throws GeneralSecurityException, JsonProcessingException {
        return new ServerSideEncryptionKms("Key-Id", contextMap);
    }
}
