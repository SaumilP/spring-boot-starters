/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.aspect;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import io.github.saumilp.starters.featureflags.annotation.FeatureEnabled;
import io.github.saumilp.starters.featureflags.exception.FeatureDisabledException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP aspect that enforces
 * {@link io.github.saumilp.starters.featureflags.annotation.FeatureEnabled @FeatureEnabled}
 * annotations by evaluating the referenced flag via the OpenFeature SDK before the method
 * is allowed to execute.
 *
 * <p>The aspect uses the {@link OpenFeatureAPI} singleton to obtain a {@link Client} and
 * evaluates the boolean flag. If the flag is {@code true}, the method proceeds normally.
 * If the flag is {@code false}:
 * <ul>
 *   <li>When {@link FeatureEnabled#disableWhenOff()} is {@code true}, a
 *       {@link FeatureDisabledException} is thrown to block execution.</li>
 *   <li>When {@link FeatureEnabled#disableWhenOff()} is {@code false} (the default),
 *       the method proceeds regardless — the annotation is advisory only.</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Aspect
public class FeatureEnabledAspect {

    private static final Logger log = LoggerFactory.getLogger(FeatureEnabledAspect.class);

    private final OpenFeatureAPI openFeatureAPI;

    /**
     * Constructs the aspect with the given {@link OpenFeatureAPI} instance.
     *
     * @param openFeatureAPI the OpenFeature API used to evaluate flags; must not be {@code null}
     */
    public FeatureEnabledAspect(OpenFeatureAPI openFeatureAPI) {
        this.openFeatureAPI = openFeatureAPI;
    }

    /**
     * Intercepts methods annotated with {@link FeatureEnabled} and enforces the flag check
     * before allowing execution to proceed.
     *
     * @param pjp           the proceeding join point; must not be {@code null}
     * @param featureEnabled the annotation carrying the flag key and enforcement options;
     *                       must not be {@code null}
     * @return the return value of the intercepted method when execution is permitted
     * @throws FeatureDisabledException if the flag is disabled and
     *                                  {@link FeatureEnabled#disableWhenOff()} is {@code true}
     * @throws Throwable                re-throws any exception raised by the intercepted method
     */
    @Around("@annotation(featureEnabled)")
    public Object checkFeatureFlag(ProceedingJoinPoint pjp, FeatureEnabled featureEnabled)
        throws Throwable {

        String flagKey = featureEnabled.flag();
        Client client  = openFeatureAPI.getClient();
        boolean enabled = client.getBooleanValue(flagKey, false);

        log.debug("Feature flag '{}' evaluated to {} for method '{}'",
            flagKey, enabled, pjp.getSignature().toShortString());

        if (!enabled && featureEnabled.disableWhenOff()) {
            throw new FeatureDisabledException(flagKey);
        }

        return pjp.proceed();
    }
}
