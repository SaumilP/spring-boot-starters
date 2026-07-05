/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.dataprivacy;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Customer}.
 *
 * @author SaumilP
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
