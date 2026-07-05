/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.aspect;

import io.github.saumilp.starters.schedulerlock.annotation.SchedulerLock;
import io.github.saumilp.starters.schedulerlock.config.SchedulerLockProperties;
import io.github.saumilp.starters.schedulerlock.lock.LockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchedulerLockAspect} using an AspectJ proxy over a target bean.
 */
class SchedulerLockAspectTest {

    private LockProvider lockProvider;
    private Job target;
    private Job proxy;

    @BeforeEach
    void setUp() {
        lockProvider = mock(LockProvider.class);
        target = new Job();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new SchedulerLockAspect(lockProvider, new SchedulerLockProperties()));
        proxy = factory.getProxy();
    }

    @Test
    void should_runTaskAndRelease_when_lockAcquired() {
        when(lockProvider.tryAcquire(eq("job"), any())).thenReturn(true);

        proxy.run();

        assertThat(target.runs).isEqualTo(1);
        verify(lockProvider).release("job");
    }

    @Test
    void should_skipTask_when_lockNotAcquired() {
        when(lockProvider.tryAcquire(any(), any())).thenReturn(false);

        proxy.run();

        assertThat(target.runs).isZero();
        verify(lockProvider, never()).release(any());
    }

    static class Job {
        int runs = 0;

        @SchedulerLock(name = "job", lockAtMostFor = "PT30S")
        public void run() {
            runs++;
        }
    }
}
