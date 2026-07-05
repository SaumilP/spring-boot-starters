/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.awss3;

import io.github.saumilp.starters.s3.service.S3StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * Demonstrates S3 upload, download, presigned URL, and delete via {@link S3StorageService}.
 *
 * @author SaumilP
 */
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3StorageService s3;
    private final String bucket;

    public S3Controller(S3StorageService s3,
                        @Value("${spring.aws.s3.bucket-name:my-bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "key", required = false) String key) throws IOException {

        String objectKey = key != null ? key : file.getOriginalFilename();
        try (InputStream in = file.getInputStream()) {
            s3.upload(bucket, objectKey, in, file.getSize(), file.getContentType());
        }
        return ResponseEntity.ok(Map.of("key", objectKey, "status", "uploaded"));
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) throws IOException {
        InputStream stream = s3.download(bucket, key);
        byte[] bytes = stream.readAllBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/presign/{key}")
    public ResponseEntity<Map<String, String>> presign(@PathVariable String key) {
        String url = s3.presignedGetUrl(bucket, key, Duration.ofHours(1));
        return ResponseEntity.ok(Map.of("presignedUrl", url, "key", key));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        s3.delete(bucket, key);
        return ResponseEntity.ok(Map.of("key", key, "status", "deleted"));
    }
}
