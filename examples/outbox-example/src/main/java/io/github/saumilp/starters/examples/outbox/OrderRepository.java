/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author SaumilP
 */
public interface OrderRepository extends JpaRepository<Order, String> {
}
