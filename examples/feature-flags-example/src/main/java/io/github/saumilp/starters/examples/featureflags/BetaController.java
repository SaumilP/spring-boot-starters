/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.featureflags;

import dev.openfeature.sdk.OpenFeatureAPI;
import io.github.saumilp.starters.featureflags.annotation.FeatureEnabled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates feature flag evaluation via {@link FeatureEnabled} and the OpenFeature SDK.
 */
@RestController
@RequestMapping("/feature")
public class BetaController {

    private final OpenFeatureAPI openFeatureAPI;

    public BetaController(OpenFeatureAPI openFeatureAPI) {
        this.openFeatureAPI = openFeatureAPI;
    }

    @GetMapping("/new-dashboard")
    @FeatureEnabled(flag = "new-dashboard", disableWhenOff = true)
    public Map<String, String> newDashboard() {
        return Map.of("ui", "new-dashboard", "status", "active");
    }

    @GetMapping("/status")
    public Map<String, Object> flagStatus() {
        var client = openFeatureAPI.getClient();
        return Map.of(
            "new-dashboard", client.getBooleanValue("new-dashboard", false),
            "beta-search",   client.getBooleanValue("beta-search", false)
        );
    }
}
