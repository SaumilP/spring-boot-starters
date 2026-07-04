/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.minio;

import io.github.saumilp.starters.services.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * REST controller demonstrating MinIO file storage operations via {@link StorageService}.
 */
@RestController
@RequestMapping("/files")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Path path = Path.of(file.getOriginalFilename());
        storageService.upload(path, file.getInputStream(),
            file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        return ResponseEntity.ok(Map.of("key", file.getOriginalFilename(), "status", "uploaded"));
    }

    @GetMapping("/{key}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key) throws Exception {
        var stream = storageService.get(Path.of(key));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(stream));
    }

    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(storageService.listFiles());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) throws Exception {
        storageService.remove(Path.of(key));
        return ResponseEntity.ok(Map.of("key", key, "status", "deleted"));
    }
}
