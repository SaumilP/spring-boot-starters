/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.dataprivacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example application demonstrating {@code spring-boot-starter-data-privacy}: JPA field-level
 * encryption of a national ID number and masked display via the masking service.
 *
 * @author SaumilP
 */
@SpringBootApplication
public class DataPrivacyExampleApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DataPrivacyExampleApplication.class, args);
    }
}
