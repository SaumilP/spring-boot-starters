/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.ratelimit;

import io.github.saumilp.starters.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates method-level and named rate limiting with {@link RateLimit}.
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    @GetMapping("/search")
    @RateLimit(requests = 5, per = TimeUnit.MINUTES)
    public Map<String, Object> search(@RequestParam(defaultValue = "spring") String q) {
        return Map.of(
            "query", q,
            "results", List.of("Result 1 for " + q, "Result 2 for " + q)
        );
    }

    @GetMapping("/admin/search")
    @RateLimit(name = "admin-search")
    public Map<String, Object> adminSearch(@RequestParam(defaultValue = "spring") String q) {
        return Map.of(
            "query", q,
            "results", List.of("Admin result 1", "Admin result 2"),
            "source", "admin"
        );
    }
}
