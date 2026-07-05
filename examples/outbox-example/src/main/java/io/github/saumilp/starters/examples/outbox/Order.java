/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.outbox;

import jakarta.persistence.*;

/**
 * @author SaumilP (email2saumil2024@gmail.com)
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String details;

    @Column(nullable = false)
    private String status;

    protected Order() {}

    public Order(String orderId, String details, String status) {
        this.orderId = orderId;
        this.details = details;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
}
