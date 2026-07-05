/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.multitenancy;

import io.github.saumilp.starters.multitenancy.context.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Returns the current tenant resolved from the {@code X-Tenant-Id} request header via {@link TenantContext}.
 */
@RestController
@RequestMapping("/tenant")
public class TenantController {

    @GetMapping("/info")
    public Map<String, String> info() {
        String tenant = TenantContext.get();
        return Map.of(
            "tenant", tenant != null ? tenant : "none",
            "status", tenant != null ? "resolved" : "not-set"
        );
    }
}
