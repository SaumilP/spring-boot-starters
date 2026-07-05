/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.multitenancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-multitenancy starter with header-based tenant resolution.
 */
@SpringBootApplication
public class MultitenancyExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultitenancyExampleApplication.class, args);
    }
}
