/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Applies a rate limit to the annotated controller method or class.
 *
 * <p>When placed on a class, the configured limit applies to every handler method within that
 * class. A method-level {@code @RateLimit} overrides any class-level annotation for that
 * specific method, allowing fine-grained control alongside a class-wide default.
 *
 * <p>The rate-limit key is resolved in the following priority order:
 * <ol>
 *   <li>The SpEL expression in {@link #key()} if non-empty — evaluated against the current
 *       {@link org.springframework.web.context.request.RequestAttributes} and method arguments</li>
 *   <li>A named limit from {@code spring.rate-limit.named-limits.*} if {@link #name()} is set</li>
 *   <li>The combination of authenticated principal (or remote IP when unauthenticated) and the
 *       fully-qualified method name</li>
 * </ol>
 *
 * <p>When the limit is exceeded, {@link io.github.saumilp.starters.ratelimit.exception.RateLimitExceededException}
 * is thrown, which the bundled {@link io.github.saumilp.starters.ratelimit.web.RateLimitExceptionHandler}
 * translates to an HTTP {@code 429 Too Many Requests} response.
 *
 * <p>Example — 100 requests per minute per IP:
 * <pre>{@code
 * @RateLimit(requests = 100, per = TimeUnit.MINUTES, key = "#request.remoteAddr")
 * @GetMapping("/api/search")
 * public ResponseEntity<List<Result>> search(@RequestParam String q) { ... }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Maximum number of requests allowed within the time window defined by {@link #per()}.
     * Must be a positive integer. Defaults to {@code 60}.
     *
     * @return the maximum request count per window
     */
    int requests() default 60;

    /**
     * The time unit defining the duration of the sliding window used to count requests.
     * Combined with {@link #requests()}, expresses the rate as {@code requests} per {@code per}.
     * Defaults to {@link TimeUnit#MINUTES}.
     *
     * @return the time unit of the sliding window
     */
    TimeUnit per() default TimeUnit.MINUTES;

    /**
     * Optional SpEL expression evaluated at request time to produce a custom rate-limit key.
     * When empty (the default), the key is derived automatically from the principal and method.
     * Example: {@code "#request.remoteAddr + ':search'"}
     *
     * @return the SpEL key expression, or an empty string to derive the key automatically
     */
    String key() default "";

    /**
     * Name of a pre-configured limit defined under {@code spring.rate-limit.named-limits.*}.
     * When specified, the {@code requests} and {@code per} attributes of this annotation are
     * ignored in favour of the named configuration. This allows centralised limit management
     * in {@code application.yml} without scattering numeric values across annotations.
     *
     * @return the named-limit key, or an empty string to use the inline {@code requests}/{@code per}
     */
    String name() default "";
}
