/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.auditlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-audit-log starter with automatic audit trail via @Audited.
 */
@SpringBootApplication
public class AuditLogExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditLogExampleApplication.class, args);
    }
}
