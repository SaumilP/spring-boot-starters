/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.configs;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Custom {@link CachingConfigurer} that generates structured cache keys for Spring's
 * {@code @Cacheable}, {@code @CachePut}, and {@code @CacheEvict} annotations.
 *
 * <p>Keys are constructed by concatenating the fully-qualified class name, method name,
 * and the string representations of all method arguments, separated by colons. This
 * guarantees uniqueness across methods with identical parameter lists in different classes.
 *
 * <p>Example key for {@code UserService.findById(42)}:
 * <pre>{@code
 * org.example.UserService:findById:42:
 * }</pre>
 *
 * <p>This implementation replaces the deprecated {@code CachingConfigurerSupport} base class
 * removed in Spring Framework 7.x.
 *
 * @since 1.0.0
 * @see CachingConfigurer
 */
public class RedisKeyGenerator implements CachingConfigurer {

    /**
     * Returns a {@link KeyGenerator} that builds cache keys from the target class name,
     * method name, and all parameter values.
     *
     * @return a non-null {@link KeyGenerator} instance shared across all cache operations
     */
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName()).append(':');
            sb.append(method.getName()).append(':');
            for (Object param : params) {
                sb.append(param).append(':');
            }
            return sb.toString();
        };
    }
}
