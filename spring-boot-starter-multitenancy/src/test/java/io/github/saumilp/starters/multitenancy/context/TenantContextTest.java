/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantContext} thread-local behaviour and propagation.
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void should_returnTenantId_when_setOnCurrentThread() {
        TenantContext.set("acme");
        assertThat(TenantContext.get()).isEqualTo("acme");
    }

    @Test
    void should_returnNull_when_neverSet() {
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void should_returnNull_after_clear() {
        TenantContext.set("acme");
        TenantContext.clear();
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void should_returnFalse_when_notSet() {
        assertThat(TenantContext.isSet()).isFalse();
    }

    @Test
    void should_returnTrue_when_set() {
        TenantContext.set("beta");
        assertThat(TenantContext.isSet()).isTrue();
    }

    @Test
    void should_propagateTenantId_to_childThread() throws InterruptedException {
        TenantContext.set("inherited-tenant");
        AtomicReference<String> childValue = new AtomicReference<>();
        Thread child = new Thread(() -> childValue.set(TenantContext.get()));
        child.start();
        child.join();
        assertThat(childValue.get()).isEqualTo("inherited-tenant");
    }
}
