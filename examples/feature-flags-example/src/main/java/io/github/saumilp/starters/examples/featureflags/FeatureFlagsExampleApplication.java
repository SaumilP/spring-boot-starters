/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.featureflags;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-feature-flags starter with file-based flag evaluation.
 */
@SpringBootApplication
public class FeatureFlagsExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagsExampleApplication.class, args);
    }
}
