/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @Scheduled} method so that, across a cluster, only one instance executes each
 * scheduled run. The lock is acquired before the method runs and released after it completes.
 *
 * <pre>{@code
 * @Scheduled(fixedRate = 60_000)
 * @SchedulerLock(name = "nightly-report", lockAtMostFor = "PT5M")
 * public void generateReport() { ... }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SchedulerLock {

    /**
     * The unique lock name shared by all instances competing for this task.
     *
     * @return the lock name
     */
    String name();

    /**
     * The maximum time the lock is held, as an ISO-8601 duration (e.g. {@code "PT5M"}). This is a
     * safety ceiling that releases the lock even if the holding instance crashes. When empty, the
     * configured default ({@code spring.scheduler-lock.default-lock-at-most-for}) is used.
     *
     * @return the ISO-8601 lock-at-most-for duration, or an empty string to use the default
     */
    String lockAtMostFor() default "";
}
