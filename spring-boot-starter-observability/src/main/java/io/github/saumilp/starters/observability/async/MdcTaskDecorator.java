/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * {@link TaskDecorator} that propagates the SLF4J {@link MDC} context (including the correlation
 * ID) from the submitting thread to the worker thread of an async executor.
 *
 * <p>Register this decorator on a {@code ThreadPoolTaskExecutor} so that logs emitted from
 * {@code @Async} methods retain the originating request's correlation ID. The worker thread's
 * previous MDC is restored after the task completes.
 *
 * @since 1.0.0
 */
public class MdcTaskDecorator implements TaskDecorator {

    /** Creates a new MDC-propagating task decorator. */
    public MdcTaskDecorator() {
    }

    /**
     * {@inheritDoc}
     *
     * @param runnable the task to wrap; must not be {@code null}
     * @return a runnable that applies the captured MDC context around {@code runnable}
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (captured != null) {
                MDC.setContextMap(captured);
            } else {
                MDC.clear();
            }
            try {
                runnable.run();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
