/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.aspect;

import io.github.saumilp.starters.schedulerlock.annotation.SchedulerLock;
import io.github.saumilp.starters.schedulerlock.config.SchedulerLockProperties;
import io.github.saumilp.starters.schedulerlock.lock.LockProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * AOP aspect that enforces {@link SchedulerLock} on annotated methods.
 *
 * <p>Before the target method runs, the aspect attempts to acquire the named lock via the
 * configured {@link LockProvider}. If the lock is acquired, the method runs and the lock is
 * released afterwards; if not, the method is skipped (another instance is running it).
 *
 * @since 1.0.0
 */
@Aspect
public class SchedulerLockAspect {

    private static final Logger log = LoggerFactory.getLogger(SchedulerLockAspect.class);

    private final LockProvider lockProvider;
    private final SchedulerLockProperties properties;

    /**
     * Constructs the aspect.
     *
     * @param lockProvider the lock provider; must not be {@code null}
     * @param properties   the scheduler-lock configuration; must not be {@code null}
     */
    public SchedulerLockAspect(LockProvider lockProvider, SchedulerLockProperties properties) {
        this.lockProvider = lockProvider;
        this.properties = properties;
    }

    /**
     * Wraps a {@link SchedulerLock}-annotated method with acquire/skip/release logic.
     *
     * @param joinPoint     the intercepted invocation; must not be {@code null}
     * @param schedulerLock the annotation on the method; must not be {@code null}
     * @return the method result, or {@code null} when the run is skipped because the lock is held
     * @throws Throwable if the target method throws
     */
    @Around("@annotation(schedulerLock)")
    public Object around(ProceedingJoinPoint joinPoint, SchedulerLock schedulerLock) throws Throwable {
        String name = schedulerLock.name();
        Duration atMostFor = resolveAtMostFor(schedulerLock);

        if (!lockProvider.tryAcquire(name, atMostFor)) {
            log.debug("Skipping scheduled task '{}' — lock held by another instance", name);
            return null;
        }
        try {
            return joinPoint.proceed();
        } finally {
            lockProvider.release(name);
        }
    }

    private Duration resolveAtMostFor(SchedulerLock schedulerLock) {
        String value = schedulerLock.lockAtMostFor();
        if (!StringUtils.hasText(value)) {
            return properties.getDefaultLockAtMostFor();
        }
        return Duration.parse(value);
    }
}
