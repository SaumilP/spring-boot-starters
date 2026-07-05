/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-minio starter with file upload/download/list/delete.
 */
@SpringBootApplication
public class MinioExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinioExampleApplication.class, args);
    }
}
