/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.outbox;

import io.github.saumilp.starters.outbox.publisher.OutboxEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Places an order and publishes an ORDER_PLACED outbox event in the same transaction.
 *
 * @author SaumilP (email2saumil2024@gmail.com)
 */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final OutboxEventPublisher outboxPublisher;

    public OrderService(OrderRepository orders, OutboxEventPublisher outboxPublisher) {
        this.orders = orders;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional
    public Order placeOrder(String orderId, String details) {
        Order order = new Order(orderId, details, "PLACED");
        orders.save(order);

        String payload = "{\"orderId\":\"" + orderId + "\",\"details\":\"" + details + "\",\"status\":\"PLACED\"}";
        outboxPublisher.publish("Order", orderId, "ORDER_PLACED", payload);

        return order;
    }
}
