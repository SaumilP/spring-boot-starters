/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.apikeys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example application demonstrating {@code spring-boot-starter-api-keys}: issuing a key and enforcing
 * it on a protected path via the auto-configured filter.
 *
 * @author SaumilP
 */
@SpringBootApplication
public class ApiKeysExampleApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiKeysExampleApplication.class, args);
    }
}
