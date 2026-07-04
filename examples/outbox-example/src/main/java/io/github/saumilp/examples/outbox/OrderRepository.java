/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author SaumilP (email2saumil2024@gmail.com)
 */
public interface OrderRepository extends JpaRepository<Order, String> {
}
