/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.config;

import io.github.saumilp.starters.schedulerlock.aspect.SchedulerLockAspect;
import io.github.saumilp.starters.schedulerlock.lock.InMemoryLockProvider;
import io.github.saumilp.starters.schedulerlock.lock.LockProvider;
import io.github.saumilp.starters.schedulerlock.lock.RedisLockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link SchedulerLockAutoConfiguration} provider selection and conditional wiring.
 */
class SchedulerLockAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SchedulerLockAutoConfiguration.class));

    @Test
    void should_useInMemoryProviderAndAspect_byDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(SchedulerLockAspect.class);
            assertThat(context).getBean(LockProvider.class).isInstanceOf(InMemoryLockProvider.class);
        });
    }

    @Test
    void should_useRedisProvider_when_selectedAndRedisPresent() {
        runner.withPropertyValues("spring.scheduler-lock.provider=redis")
            .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
            .run(context -> assertThat(context)
                .getBean(LockProvider.class).isInstanceOf(RedisLockProvider.class));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.scheduler-lock.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(SchedulerLockAspect.class)
                .doesNotHaveBean(LockProvider.class));
    }
}
