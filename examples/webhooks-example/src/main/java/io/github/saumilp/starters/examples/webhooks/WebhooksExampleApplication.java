/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.webhooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example application demonstrating {@code spring-boot-starter-webhooks}: registering subscriber
 * endpoints and delivering signed, retrying events to them.
 *
 * @author SaumilP
 */
@SpringBootApplication
public class WebhooksExampleApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WebhooksExampleApplication.class, args);
    }
}
