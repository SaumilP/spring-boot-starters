/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.idempotency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-idempotency starter with a POST endpoint
 * that replays cached responses for duplicate requests.
 */
@SpringBootApplication
public class IdempotencyExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdempotencyExampleApplication.class, args);
    }
}
