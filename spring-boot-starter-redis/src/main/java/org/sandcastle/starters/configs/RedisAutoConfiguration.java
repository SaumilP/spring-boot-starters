/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.configs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.sandcastle.starters.properties.RedisConfigurationProperties;
import org.sandcastle.starters.utils.RedisLockUtil;
import org.sandcastle.starters.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring Boot auto-configuration for the Redis starter.
 *
 * <p>Registers the following beans when a {@link RedisConnectionFactory} is present on the
 * classpath:
 * <ul>
 *   <li>{@link RedisTemplate} — a pre-configured template with Jackson JSON serialisation
 *       for values and {@link StringRedisSerializer} for keys</li>
 *   <li>{@link StringRedisTemplate} — for plain string key/value operations</li>
 *   <li>{@link CacheManager} — a Redis-backed Spring cache manager with configurable TTL</li>
 *   <li>{@link CachingConfigurer} — provides the structured key generator</li>
 *   <li>{@link RedisUtil} — high-level utility wrapping common Redis operations</li>
 *   <li>{@link RedisLockUtil} — distributed lock implementation using a Lua script</li>
 * </ul>
 *
 * <p>All beans are annotated with {@link ConditionalOnMissingBean} so consuming applications
 * can replace any individual bean by declaring their own.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
@EnableConfigurationProperties(RedisConfigurationProperties.class)
public class RedisAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisAutoConfiguration.class);

    /**
     * Provides the custom cache key generator as a {@link CachingConfigurer}.
     *
     * @return a {@link RedisKeyGenerator} instance; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(CachingConfigurer.class)
    public CachingConfigurer cachingConfigurer() {
        return new RedisKeyGenerator();
    }

    /**
     * Creates the Spring {@link CacheManager} backed by Redis, with TTL configured from
     * {@link RedisConfigurationProperties#getCacheTtlDays()}.
     *
     * @param connectionFactory the Redis connection factory provided by Spring Data Redis
     * @param props             the starter configuration properties
     * @return a configured {@link RedisCacheManager}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     RedisConfigurationProperties props) {
        RedisCacheWriter writer = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(props.getCacheTtlDays()))
                .disableCachingNullValues();

        log.debug("Configuring Redis CacheManager with TTL of {} day(s)", props.getCacheTtlDays());
        return new RedisCacheManager(writer, config);
    }

    /**
     * Creates the primary {@link RedisTemplate} with Jackson JSON value serialisation and
     * {@link StringRedisSerializer} key serialisation.
     *
     * @param connectionFactory the Redis connection factory; must not be {@code null}
     * @return a fully initialised {@link RedisTemplate}; never {@code null}
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(buildObjectMapper(), Object.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a {@link StringRedisTemplate} for operations that store and retrieve plain strings.
     *
     * @param connectionFactory the Redis connection factory; must not be {@code null}
     * @return a configured {@link StringRedisTemplate}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * Creates the domain-specific Redis template used for storing complex domain objects.
     * Registers as a secondary template alongside the primary {@link #redisTemplate}.
     *
     * @param connectionFactory the Redis connection factory; must not be {@code null}
     * @return a configured {@link RedisTemplate}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(name = "functionDomainRedisTemplate")
    public RedisTemplate<String, Object> functionDomainRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(buildObjectMapper());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setValueSerializer(serializer);
        template.setEnableTransactionSupport(true);
        template.setConnectionFactory(connectionFactory);
        log.info("Redis cache service initialised successfully.");
        return template;
    }

    /**
     * Provides the high-level {@link RedisUtil} wrapping common Redis data-structure operations.
     *
     * @param redisTemplate the primary Redis template; must not be {@code null}
     * @param redisLockUtil the distributed lock utility; must not be {@code null}
     * @return a configured {@link RedisUtil} instance; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RedisUtil.class)
    public RedisUtil redisUtils(RedisTemplate<String, Object> redisTemplate,
                                RedisLockUtil redisLockUtil) {
        return new RedisUtil(redisTemplate, redisLockUtil);
    }

    /**
     * Provides the {@link RedisLockUtil} implementing distributed lock acquisition and
     * release via an atomic Lua script.
     *
     * @param redisTemplate the primary Redis template; must not be {@code null}
     * @return a configured {@link RedisLockUtil} instance; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(RedisLockUtil.class)
    public RedisLockUtil redisLockUtil(RedisTemplate<String, Object> redisTemplate) {
        return new RedisLockUtil(redisTemplate);
    }

    /**
     * Constructs a shared {@link ObjectMapper} configured for Redis serialisation.
     * Enables default typing to preserve concrete types during deserialisation, disables
     * timestamp-based date serialisation in favour of ISO-8601 strings, and registers
     * the JSR-310, JDK 8, and parameter-names modules.
     *
     * @return a fully configured {@link ObjectMapper}; never {@code null}
     */
    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new ParameterNamesModule());
        return mapper;
    }
}
