/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guards a Spring-managed method or controller handler behind a feature flag evaluated
 * at runtime via the OpenFeature SDK.
 *
 * <p>When the annotated method is invoked, the
 * {@link io.github.saumilp.starters.featureflags.aspect.FeatureEnabledAspect} intercepts
 * the call and evaluates the flag identified by {@link #flag()} against the currently
 * registered {@link dev.openfeature.sdk.FeatureProvider}. The behaviour when the flag is
 * {@code false} depends on the combination of {@link #disableWhenOff()} and
 * {@link #fallbackUrl()}:
 *
 * <ul>
 *   <li>If {@code disableWhenOff = true}, an
 *       {@link io.github.saumilp.starters.featureflags.exception.FeatureDisabledException}
 *       is thrown, which can be handled by a {@code @ControllerAdvice} to return an
 *       appropriate HTTP response.</li>
 *   <li>If {@code disableWhenOff = false} (the default), the method proceeds regardless —
 *       the annotation is advisory, useful for gradual rollouts or canary analysis.</li>
 * </ul>
 *
 * <p>Example — hard-gate a new checkout flow:
 * <pre>{@code
 * @FeatureEnabled(flag = "new-checkout", disableWhenOff = true)
 * @PostMapping("/checkout/v2")
 * public ResponseEntity<OrderConfirmation> checkoutV2(@RequestBody CheckoutRequest req) { ... }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureEnabled {

    /**
     * The feature flag key to evaluate. Must match a key known to the configured
     * {@link dev.openfeature.sdk.FeatureProvider}.
     *
     * @return the flag key; must not be blank
     */
    String flag();

    /**
     * An optional URL to redirect to when the flag is disabled. Only meaningful in web
     * application contexts where the response is an HTTP redirect. If left empty (the
     * default), no redirect is performed and behaviour is determined by
     * {@link #disableWhenOff()}.
     *
     * @return the fallback URL; empty string means no redirect
     */
    String fallbackUrl() default "";

    /**
     * When {@code true}, the aspect throws
     * {@link io.github.saumilp.starters.featureflags.exception.FeatureDisabledException}
     * if the flag evaluates to {@code false}, preventing the method from executing.
     * When {@code false} (the default), the method proceeds even if the flag is off.
     *
     * @return {@code true} to hard-gate the method; {@code false} for advisory-only mode
     */
    boolean disableWhenOff() default false;
}
