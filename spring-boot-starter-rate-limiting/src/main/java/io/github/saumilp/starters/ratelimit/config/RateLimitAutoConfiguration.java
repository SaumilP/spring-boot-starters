/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.config;

import io.github.saumilp.starters.ratelimit.aspect.RateLimitAspect;
import io.github.saumilp.starters.ratelimit.service.InMemorySlidingWindowRateLimiter;
import io.github.saumilp.starters.ratelimit.service.RateLimiter;
import io.github.saumilp.starters.ratelimit.service.RedisTokenBucketRateLimiter;
import io.github.saumilp.starters.ratelimit.web.RateLimitExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Spring Boot auto-configuration for the rate-limiting starter.
 *
 * <p>Registers the following beans when {@code spring.rate-limit.enabled} is {@code true}
 * (the default):
 * <ul>
 *   <li>{@link RateLimiter} — Redis-backed ({@link RedisTokenBucketRateLimiter}) when
 *       {@link RedisTemplate} is on the classpath; falls back to an in-memory sliding window
 *       ({@link InMemorySlidingWindowRateLimiter}) otherwise</li>
 *   <li>{@link RateLimitAspect} — AOP interceptor that enforces {@link
 *       io.github.saumilp.starters.ratelimit.annotation.RateLimit} annotations</li>
 *   <li>{@link RateLimitExceptionHandler} — converts {@link
 *       io.github.saumilp.starters.ratelimit.exception.RateLimitExceededException} to HTTP 429
 *       in servlet web contexts</li>
 * </ul>
 *
 * <p>To disable rate limiting entirely:
 * <pre>{@code
 * spring:
 *   rate-limit:
 *     enabled: false
 * }</pre>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    /** Creates the rate-limiting auto-configuration. */
    public RateLimitAutoConfiguration() {
    }

    /**
     * Registers a Redis-backed {@link RateLimiter} when {@link RedisTemplate} is present on
     * the classpath and no other {@link RateLimiter} bean has been defined.
     *
     * @param redisTemplate the Spring Data Redis template; must not be {@code null}
     * @return a {@link RedisTokenBucketRateLimiter} instance; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnClass(RedisTemplate.class)
    public RateLimiter redisRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        return new RedisTokenBucketRateLimiter(redisTemplate);
    }

    /**
     * Registers an in-memory {@link RateLimiter} as a fallback when no other {@link RateLimiter}
     * bean is present. This limiter is local to the JVM and is not suitable for multi-instance
     * deployments.
     *
     * @return an {@link InMemorySlidingWindowRateLimiter} instance; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter inMemoryRateLimiter() {
        return new InMemorySlidingWindowRateLimiter();
    }

    /**
     * Registers the {@link RateLimitAspect} that enforces rate limits on annotated methods.
     *
     * @param rateLimiter the active rate-limiter implementation; must not be {@code null}
     * @param props       the starter configuration properties; must not be {@code null}
     * @return a configured {@link RateLimitAspect}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitAspect.class)
    public RateLimitAspect rateLimitAspect(RateLimiter rateLimiter, RateLimitProperties props) {
        return new RateLimitAspect(rateLimiter, props);
    }

    /**
     * Registers the {@link RateLimitExceptionHandler} in servlet-based web application contexts.
     *
     * @return a configured exception handler; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitExceptionHandler.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RateLimitExceptionHandler rateLimitExceptionHandler() {
        return new RateLimitExceptionHandler();
    }
}
