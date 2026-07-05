/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.github.saumilp.starters.common.metrics.MeterRegistryUtils;
import io.github.saumilp.starters.properties.RedisConfigurationProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP aspect that records Micrometer {@link Timer} metrics for every public method
 * on {@link io.github.saumilp.starters.utils.RedisUtil}.
 *
 * <p>Each method invocation is timed and tagged with:
 * <ul>
 *   <li>{@code operation} — the method name (e.g. {@code set}, {@code get}, {@code expire})</li>
 *   <li>{@code status} — {@code success} or {@code error}</li>
 * </ul>
 *
 * <p>The metric name is taken from
 * {@link RedisConfigurationProperties#getMetricName()} (default: {@code redis.operations}).
 * This allows consuming applications to namespace their metrics without forking the aspect.
 *
 * <p>This aspect is only activated when {@code spring-aop} and {@code aspectjweaver} are
 * present at runtime, and when a {@link MeterRegistry} bean is available in the
 * application context (guarded by {@code @ConditionalOnClass} in the auto-configuration).
 *
 * @since 1.0.0
 */
@Aspect
public class RedisMetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(RedisMetricsAspect.class);

    private final MeterRegistry meterRegistry;
    private final String metricName;

    /**
     * Constructs a {@code RedisMetricsAspect} with the given registry and configuration.
     *
     * @param meterRegistry the Micrometer registry to record timings into; must not be {@code null}
     * @param props         the Redis starter configuration properties; must not be {@code null}
     */
    public RedisMetricsAspect(MeterRegistry meterRegistry, RedisConfigurationProperties props) {
        this.meterRegistry = meterRegistry;
        this.metricName = props.getMetricName();
    }

    /**
     * Intercepts all public methods declared on {@link io.github.saumilp.starters.utils.RedisUtil},
     * records elapsed time as a Micrometer {@link Timer}, and tags the sample with the
     * operation name and outcome status.
     *
     * @param joinPoint the intercepted method invocation; must not be {@code null}
     * @return the value returned by the intercepted method
     * @throws Throwable re-throws any exception raised by the intercepted method
     */
    @Around("execution(public * io.github.saumilp.starters.utils.RedisUtil.*(..))")
    public Object recordOperationMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        Timer.Sample sample = Timer.start(meterRegistry);
        String status = MeterRegistryUtils.STATUS_SUCCESS;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            status = MeterRegistryUtils.STATUS_ERROR;
            log.debug("Redis operation '{}' failed: {}", operation, ex.getMessage());
            throw ex;
        } finally {
            sample.stop(Timer.builder(metricName)
                    .tag(MeterRegistryUtils.TAG_OPERATION, operation)
                    .tag(MeterRegistryUtils.TAG_STATUS, status)
                    .description("Redis operation execution time")
                    .register(meterRegistry));
        }
    }
}
