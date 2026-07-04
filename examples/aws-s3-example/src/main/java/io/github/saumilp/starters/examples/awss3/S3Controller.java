/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.awss3;

import io.github.saumilp.starters.awss3.service.S3StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * Demonstrates S3 upload, download, presigned URL, and delete via {@link S3StorageService}.
 *
 * @author SaumilP (email2saumil2024@gmail.com)
 */
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3StorageService s3;

    public S3Controller(S3StorageService s3) {
        this.s3 = s3;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "key", required = false) String key) throws IOException {

        String objectKey = key != null ? key : file.getOriginalFilename();
        try (InputStream in = file.getInputStream()) {
            s3.upload(objectKey, in, file.getSize(), file.getContentType());
        }
        return ResponseEntity.ok(Map.of("key", objectKey, "status", "uploaded"));
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) throws IOException {
        InputStream stream = s3.download(key);
        byte[] bytes = stream.readAllBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/presign/{key}")
    public ResponseEntity<Map<String, String>> presign(@PathVariable String key) {
        URI url = s3.presignedGetUrl(key);
        return ResponseEntity.ok(Map.of("presignedUrl", url.toString(), "key", key));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        s3.delete(key);
        return ResponseEntity.ok(Map.of("key", key, "status", "deleted"));
    }
}
