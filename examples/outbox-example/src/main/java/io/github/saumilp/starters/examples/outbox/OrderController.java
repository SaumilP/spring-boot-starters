/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.outbox;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * @author SaumilP
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody Map<String, String> body) {
        String orderId = body.getOrDefault("orderId", UUID.randomUUID().toString());
        String details = body.getOrDefault("details", "");
        Order order = orderService.placeOrder(orderId, details);
        return ResponseEntity.ok(Map.of(
            "orderId", order.getOrderId(),
            "status", order.getStatus(),
            "message", "Order placed and outbox event queued"
        ));
    }
}
