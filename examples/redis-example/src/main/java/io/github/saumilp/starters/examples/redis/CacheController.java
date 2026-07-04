/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.redis;

import io.github.saumilp.starters.utils.RedisUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller demonstrating Redis key/value cache operations via {@link RedisUtil}.
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    private final RedisUtil redisUtil;

    public CacheController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String key) {
        Object value = redisUtil.get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> put(@PathVariable String key,
                                                    @RequestBody String value) {
        redisUtil.set(key, value);
        return ResponseEntity.ok(Map.of("key", key, "status", "stored"));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        redisUtil.del(key);
        return ResponseEntity.ok(Map.of("key", key, "status", "deleted"));
    }
}
