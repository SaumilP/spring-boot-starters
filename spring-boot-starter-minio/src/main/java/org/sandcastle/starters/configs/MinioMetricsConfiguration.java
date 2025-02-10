package org.sandcastle.starters.configs;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.sandcastle.starters.properties.MinioConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;

@Aspect
@Configuration
@ConditionalOnClass({ MinioClient.class, ManagementContextAutoConfiguration.class })
@ConditionalOnEnabledHealthIndicator("minio")
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
@AutoConfigureAfter(MinioAutoConfiguration.class)
public class MinioMetricsConfiguration {

    private final MeterRegistry meterRegistry;
    private final MinioConfigurationProperties props;

    private Timer listObjectsOkTimer;
    private Timer listObjectsKoTimer;
    private Timer getObjectOkTimer;
    private Timer getObjectKoTimer;
    private Timer bucketExistsOkTimer;
    private Timer bucketExistsKoTimer;
    private Timer createBucketOkTimer;
    private Timer createBucketKoTimer;
    private Timer putObjectOkTimer;
    private Timer putObjectKoTimer;

    @Autowired
    public MinioMetricsConfiguration(MeterRegistry meterRegistry, MinioConfigurationProperties props) {
        this.meterRegistry = meterRegistry;
        this.props = props;
    }

    @PostConstruct
    public void initializeTimers() {
        this.listObjectsOkTimer = aNewTimer("listObjects", "ok");
        this.listObjectsKoTimer = aNewTimer("listObjects", "ko");
        this.getObjectOkTimer = aNewTimer("getObject", "ok");
        this.getObjectKoTimer = aNewTimer("getObject", "ko");
        this.bucketExistsOkTimer = aNewTimer("bucketExists", "ok");
        this.bucketExistsKoTimer = aNewTimer("bucketExists", "ko");
        this.createBucketOkTimer = aNewTimer("createBucket", "ok");
        this.createBucketKoTimer = aNewTimer("createBucket", "ko");
        this.putObjectOkTimer = aNewTimer("putObject", "ok");
        this.putObjectKoTimer = aNewTimer("putObject", "ko");
    }

    @ConditionalOnBean(MinioClient.class)
    @Around("execution(* io.minio.MinioClient.listObjects(..))")
    public Object listObjectsMeter(ProceedingJoinPoint pjp) throws Throwable {
        return wrapExecution(listObjectsOkTimer, listObjectsKoTimer, pjp);
    }

    @ConditionalOnBean(MinioClient.class)
    @Around("execution(* io.minio.MinioClient.getObject(..))")
    public Object getObjectMeter(ProceedingJoinPoint pjp) throws Throwable {
        return wrapExecution(getObjectOkTimer, getObjectKoTimer, pjp);
    }

    @ConditionalOnBean(MinioClient.class)
    @Around("execution(* io.minio.MinioClient.bucketExists(..))")
    public Object bucketExistsMeter(ProceedingJoinPoint pjp) throws Throwable {
        return wrapExecution(bucketExistsOkTimer, bucketExistsKoTimer, pjp);
    }

    @ConditionalOnBean(MinioClient.class)
    @Around("execution(* io.minio.MinioClient.createBucket(..))")
    public Object createBucketMeter(ProceedingJoinPoint pjp) throws Throwable {
        return wrapExecution(createBucketOkTimer, createBucketKoTimer, pjp);
    }

    @ConditionalOnBean(MinioClient.class)
    @Around("execution(* io.minio.MinioClient.putObject(..))")
    public Object putObjectMeter(ProceedingJoinPoint pjp) throws Throwable {
        return wrapExecution(putObjectOkTimer, putObjectKoTimer, pjp);
    }

    private Timer aNewTimer(String operation, String status) {
        return Timer.builder(props.getMetricName())
                .tag("operation", operation)
                .tag("status", status)
                .tag("bucket", props.getBucket())
                .register(meterRegistry);
    }

    private Object wrapExecution(Timer okTimer, Timer koTimer, ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            var proceed = pjp.proceed();
            okTimer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            return proceed;
        } catch (Exception ex) {
            koTimer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            throw ex;
        }
    }
}