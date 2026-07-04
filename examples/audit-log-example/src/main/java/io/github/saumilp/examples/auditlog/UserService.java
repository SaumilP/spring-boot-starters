/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.auditlog;

import io.github.saumilp.starters.auditlog.annotation.Audited;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Demonstrates automatic audit trail generation via the {@link Audited} annotation.
 * Every call to {@link #createUser} and {@link #deleteUser} is captured in the audit log.
 */
@Service
public class UserService {

    @Audited(action = "CREATE_USER", resource = "User", resourceIdExpression = "#name")
    public Map<String, String> createUser(String name) {
        return Map.of(
            "id",   UUID.randomUUID().toString(),
            "name", name,
            "status", "created"
        );
    }

    @Audited(action = "DELETE_USER", resource = "User", resourceIdExpression = "#id")
    public Map<String, String> deleteUser(String id) {
        return Map.of("id", id, "status", "deleted");
    }
}
