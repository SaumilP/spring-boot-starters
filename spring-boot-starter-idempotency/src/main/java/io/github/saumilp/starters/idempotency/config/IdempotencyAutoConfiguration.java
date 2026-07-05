/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.config;

import io.github.saumilp.starters.idempotency.store.IdempotencyStore;
import io.github.saumilp.starters.idempotency.store.RedisIdempotencyStore;
import io.github.saumilp.starters.idempotency.web.IdempotencyFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Spring Boot auto-configuration for the idempotency starter.
 *
 * <p>Registers the following beans when active:
 * <ul>
 *   <li>{@link IdempotencyStore} — a Redis-backed store using {@link StringRedisTemplate}</li>
 *   <li>{@link IdempotencyFilter} — the servlet filter wrapped in a
 *       {@link FilterRegistrationBean} registered at {@link Ordered#HIGHEST_PRECEDENCE}{@code + 10}</li>
 * </ul>
 *
 * <p>The entire configuration activates only in web application contexts
 * ({@link ConditionalOnWebApplication}) and can be disabled via:
 * <pre>{@code spring.idempotency.enabled=false}</pre>
 *
 * <p>Consuming applications can replace either bean by declaring their own.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(
    prefix = "spring.idempotency",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyAutoConfiguration {

    /** Creates the idempotency auto-configuration. */
    public IdempotencyAutoConfiguration() {
    }

    /**
     * Registers the Redis-backed {@link IdempotencyStore}.
     *
     * @param redisTemplate the Spring Data Redis string template; must not be {@code null}
     * @param props         the starter configuration properties; must not be {@code null}
     * @return a configured {@link RedisIdempotencyStore}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    public IdempotencyStore idempotencyStore(StringRedisTemplate redisTemplate,
                                             IdempotencyProperties props) {
        return new RedisIdempotencyStore(redisTemplate, props.getKeyPrefix());
    }

    /**
     * Registers the {@link IdempotencyFilter} with a high-precedence order so it runs
     * before most application filters. Registered at {@link Ordered#HIGHEST_PRECEDENCE}{@code + 10}
     * to leave room for security filters that must run first (e.g., authentication).
     *
     * @param store the idempotency store; must not be {@code null}
     * @param props the starter configuration properties; must not be {@code null}
     * @return a {@link FilterRegistrationBean} wrapping the idempotency filter; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyFilter.class)
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(IdempotencyStore store,
                                                                        IdempotencyProperties props) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdempotencyFilter(store, props));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("idempotencyFilter");
        return registration;
    }
}
