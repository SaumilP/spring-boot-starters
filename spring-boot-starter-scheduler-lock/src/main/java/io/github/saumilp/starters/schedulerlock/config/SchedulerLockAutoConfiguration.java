/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.config;

import io.github.saumilp.starters.schedulerlock.aspect.SchedulerLockAspect;
import io.github.saumilp.starters.schedulerlock.lock.InMemoryLockProvider;
import io.github.saumilp.starters.schedulerlock.lock.LockProvider;
import io.github.saumilp.starters.schedulerlock.lock.RedisLockProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Auto-configuration for the scheduler-lock starter.
 *
 * <p>Registers a {@link LockProvider} (a Redis-backed one when {@code provider=redis} and a
 * {@link StringRedisTemplate} is available, otherwise an in-memory fallback) and the
 * {@link SchedulerLockAspect} that enforces the {@code @SchedulerLock} annotation.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(ProceedingJoinPoint.class)
@EnableConfigurationProperties(SchedulerLockProperties.class)
@ConditionalOnProperty(prefix = "spring.scheduler-lock", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class SchedulerLockAutoConfiguration {

    /** Creates the scheduler-lock auto-configuration. */
    public SchedulerLockAutoConfiguration() {
    }

    /**
     * Registers the in-memory lock provider unless another {@link LockProvider} (e.g. Redis) is
     * already defined.
     *
     * @return an {@link InMemoryLockProvider}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(prefix = "spring.scheduler-lock", name = "provider",
        havingValue = "in-memory", matchIfMissing = true)
    public LockProvider inMemoryLockProvider() {
        return new InMemoryLockProvider();
    }

    /**
     * Registers the {@link SchedulerLockAspect}.
     *
     * @param lockProvider the lock provider; must not be {@code null}
     * @param properties   the scheduler-lock configuration; must not be {@code null}
     * @return a {@link SchedulerLockAspect}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public SchedulerLockAspect schedulerLockAspect(LockProvider lockProvider,
                                                   SchedulerLockProperties properties) {
        return new SchedulerLockAspect(lockProvider, properties);
    }

    /**
     * Registers the Redis-backed lock provider when {@code provider=redis} and Spring Data Redis is
     * on the classpath.
     *
     * @since 1.0.0
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "spring.scheduler-lock", name = "provider", havingValue = "redis")
    static class RedisLockConfiguration {

        /** Creates the Redis lock configuration. */
        RedisLockConfiguration() {
        }

        /**
         * Registers the Redis lock provider.
         *
         * @param redisTemplate the Redis string template; must not be {@code null}
         * @param properties    the scheduler-lock configuration; must not be {@code null}
         * @return a {@link RedisLockProvider}; never {@code null}
         */
        @Bean
        @ConditionalOnMissingBean(LockProvider.class)
        LockProvider redisLockProvider(StringRedisTemplate redisTemplate,
                                       SchedulerLockProperties properties) {
            return new RedisLockProvider(redisTemplate, properties.getRedisKeyPrefix());
        }
    }
}
