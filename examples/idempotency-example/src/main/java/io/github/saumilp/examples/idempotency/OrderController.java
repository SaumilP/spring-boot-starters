/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.idempotency;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Demonstrates idempotent order creation. Sending the same {@code Idempotency-Key} twice
 * returns the cached response without executing the handler again.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> body)
            throws InterruptedException {
        // Simulate processing time to make the idempotency caching clearly visible
        Thread.sleep(100);

        String orderId = UUID.randomUUID().toString();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "orderId",   orderId,
            "item",      body.getOrDefault("item", "unknown"),
            "quantity",  body.getOrDefault("quantity", "1"),
            "createdAt", Instant.now().toString()
        ));
    }
}
