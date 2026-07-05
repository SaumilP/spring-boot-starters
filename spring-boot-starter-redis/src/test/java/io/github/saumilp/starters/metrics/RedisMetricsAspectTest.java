/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.saumilp.starters.properties.RedisConfigurationProperties;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RedisMetricsAspect}.
 */
class RedisMetricsAspectTest {

    private MeterRegistry meterRegistry;
    private RedisMetricsAspect aspect;
    private ProceedingJoinPoint joinPoint;
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        meterRegistry = new SimpleMeterRegistry();
        RedisConfigurationProperties props = new RedisConfigurationProperties();
        aspect = new RedisMetricsAspect(meterRegistry, props);

        joinPoint = mock(ProceedingJoinPoint.class);
        methodSignature = mock(MethodSignature.class);
        Method method = String.class.getMethod("length");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    @Test
    void should_recordSuccessTimer_when_operationCompletes() throws Throwable {
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.recordOperationMetrics(joinPoint);

        assertThat(result).isEqualTo("result");
        assertThat(meterRegistry.find("redis.operations")
                .tag("status", "success")
                .timer()).isNotNull();
    }

    @Test
    void should_recordErrorTimer_when_operationThrows() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Redis down"));

        assertThatThrownBy(() -> aspect.recordOperationMetrics(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis down");

        assertThat(meterRegistry.find("redis.operations")
                .tag("status", "error")
                .timer()).isNotNull();
    }

    @Test
    void should_tagOperationName_when_methodNameResolved() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        aspect.recordOperationMetrics(joinPoint);

        assertThat(meterRegistry.find("redis.operations")
                .tag("operation", "length")
                .timer()).isNotNull();
    }

    @Test
    void should_useCustomMetricName_when_configuredViaProperties() throws Throwable {
        RedisConfigurationProperties props = new RedisConfigurationProperties();
        props.setMetricName("custom.redis.ops");
        RedisMetricsAspect customAspect = new RedisMetricsAspect(meterRegistry, props);
        when(joinPoint.proceed()).thenReturn(null);

        customAspect.recordOperationMetrics(joinPoint);

        assertThat(meterRegistry.find("custom.redis.ops").timer()).isNotNull();
    }
}
