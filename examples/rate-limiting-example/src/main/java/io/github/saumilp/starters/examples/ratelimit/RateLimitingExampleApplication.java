/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.ratelimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-rate-limiting starter with per-IP and named limits.
 */
@SpringBootApplication
public class RateLimitingExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitingExampleApplication.class, args);
    }
}
