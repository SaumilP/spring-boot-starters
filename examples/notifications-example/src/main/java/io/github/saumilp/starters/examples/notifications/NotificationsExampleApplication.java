/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example application demonstrating {@code spring-boot-starter-notifications}: sending a message
 * through the {@code NotificationService} façade, delivered by the built-in logging sender.
 *
 * @author SaumilP
 */
@SpringBootApplication
public class NotificationsExampleApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationsExampleApplication.class, args);
    }
}
