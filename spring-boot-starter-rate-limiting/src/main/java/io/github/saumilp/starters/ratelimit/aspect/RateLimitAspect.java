/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import io.github.saumilp.starters.ratelimit.annotation.RateLimit;
import io.github.saumilp.starters.ratelimit.config.RateLimitProperties;
import io.github.saumilp.starters.ratelimit.exception.RateLimitExceededException;
import io.github.saumilp.starters.ratelimit.service.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * AOP aspect that intercepts methods annotated with {@link RateLimit} and enforces
 * the configured request limit using the injected {@link RateLimiter} implementation.
 *
 * <p>Key resolution order:
 * <ol>
 *   <li>Custom key expression from {@link RateLimit#key()} if non-empty (used as-is, not SpEL-evaluated
 *       in this version)</li>
 *   <li>Named limit from {@code spring.rate-limit.named-limits} if {@link RateLimit#name()} is set</li>
 *   <li>Remote IP address combined with the fully-qualified method name as the default composite key</li>
 * </ol>
 *
 * <p>If the rate limiter rejects the request,
 * {@link RateLimitExceededException} is thrown. The bundled
 * {@link io.github.saumilp.starters.ratelimit.web.RateLimitExceptionHandler} converts this to
 * an HTTP {@code 429 Too Many Requests} response.
 *
 * @since 1.0.0
 */
@Aspect
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimiter rateLimiter;
    private final RateLimitProperties props;

    /**
     * Constructs the aspect with the active rate limiter and configuration properties.
     *
     * @param rateLimiter the rate limiter to use for token consumption; must not be {@code null}
     * @param props       the starter configuration properties; must not be {@code null}
     */
    public RateLimitAspect(RateLimiter rateLimiter, RateLimitProperties props) {
        this.rateLimiter = rateLimiter;
        this.props       = props;
    }

    /**
     * Intercepts method invocations annotated with {@link RateLimit} (at method or type level)
     * and enforces the configured limit before allowing the call to proceed.
     *
     * @param pjp the proceeding join point for the intercepted method; must not be {@code null}
     * @return the return value of the intercepted method if the rate limit has not been exceeded
     * @throws RateLimitExceededException if the request count for the resolved key has reached
     *                                    the configured maximum within the current window
     * @throws Throwable                  re-throws any exception raised by the intercepted method
     */
    @Around("@annotation(io.github.saumilp.starters.ratelimit.annotation.RateLimit) || " +
            "@within(io.github.saumilp.starters.ratelimit.annotation.RateLimit)")
    public Object enforce(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        RateLimit annotation = method.getAnnotation(RateLimit.class);
        if (annotation == null) {
            annotation = pjp.getTarget().getClass().getAnnotation(RateLimit.class);
        }
        if (annotation == null) {
            return pjp.proceed();
        }

        int  maxRequests   = annotation.requests();
        long windowSeconds = annotation.per().toSeconds(1);

        if (!annotation.name().isEmpty()) {
            RateLimitProperties.NamedLimit named = props.getNamedLimits().get(annotation.name());
            if (named != null) {
                maxRequests   = named.getRequests();
                windowSeconds = named.getWindowSeconds();
            }
        }

        String key = props.getKeyPrefix() + resolveKey(pjp, annotation);
        log.debug("Checking rate limit for key='{}', max={}, window={}s", key, maxRequests, windowSeconds);

        if (!rateLimiter.tryConsume(key, maxRequests, windowSeconds)) {
            throw new RateLimitExceededException(key, maxRequests, windowSeconds);
        }

        return pjp.proceed();
    }

    /**
     * Resolves the rate-limit bucket key for the current request.
     *
     * <p>Uses the remote IP address and fully-qualified method name as the default key when
     * the annotation does not specify a custom key expression.
     *
     * @param pjp        the join point providing access to the target method
     * @param annotation the resolved {@link RateLimit} annotation
     * @return a non-null, non-blank key string
     */
    private String resolveKey(ProceedingJoinPoint pjp, RateLimit annotation) {
        if (!annotation.key().isEmpty()) {
            return annotation.key();
        }
        String ip = "unknown";
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            ip = request.getRemoteAddr();
        } catch (Exception ignored) {
            // Non-web context — fall back to "unknown"
        }
        String qualifiedMethod = pjp.getTarget().getClass().getName() + "." +
                                 pjp.getSignature().getName();
        return ip + ":" + qualifiedMethod;
    }
}
