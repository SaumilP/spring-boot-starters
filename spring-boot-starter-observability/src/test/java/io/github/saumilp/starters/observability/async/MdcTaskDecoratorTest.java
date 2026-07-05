/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MdcTaskDecorator}.
 */
class MdcTaskDecoratorTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_propagateMdc_toWorkerThread() throws Exception {
        MDC.put("correlationId", "xyz-1");
        AtomicReference<String> seen = new AtomicReference<>();

        Runnable decorated = new MdcTaskDecorator().decorate(() -> seen.set(MDC.get("correlationId")));
        Thread worker = new Thread(decorated);
        worker.start();
        worker.join();

        assertThat(seen.get()).isEqualTo("xyz-1");
    }

    @Test
    void should_restoreWorkerThreadMdc_afterRun() {
        AtomicReference<String> insideRun = new AtomicReference<>();
        Runnable decorated = new MdcTaskDecorator().decorate(() -> insideRun.set(MDC.get("correlationId")));

        // No MDC bound on this (submitting) thread → captured map is empty.
        decorated.run();

        assertThat(insideRun.get()).isNull();
        assertThat(MDC.get("correlationId")).isNull();
    }
}
