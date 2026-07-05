/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.exception;

import io.github.saumilp.starters.common.exception.StarterException;

/**
 * Thrown by the
 * {@link io.github.saumilp.starters.featureflags.aspect.FeatureEnabledAspect} when a
 * method annotated with
 * {@link io.github.saumilp.starters.featureflags.annotation.FeatureEnabled
 * @FeatureEnabled(disableWhenOff = true)} is invoked while the flag evaluates to
 * {@code false}.
 *
 * <p>Consuming applications can handle this exception in a {@code @ControllerAdvice} to
 * return an appropriate HTTP response (e.g., {@code 404 Not Found} or
 * {@code 503 Service Unavailable}):
 *
 * <pre>{@code
 * @ExceptionHandler(FeatureDisabledException.class)
 * public ResponseEntity<Void> handleDisabledFeature(FeatureDisabledException ex) {
 *     return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class FeatureDisabledException extends StarterException {

    /** The key of the feature flag that was disabled. */
    private final String flagKey;

    /**
     * Constructs a {@code FeatureDisabledException} for the given flag key.
     *
     * @param flagKey the feature flag key that evaluated to {@code false};
     *                must not be {@code null} or blank
     */
    public FeatureDisabledException(String flagKey) {
        super(String.format("Feature flag '%s' is disabled.", flagKey));
        this.flagKey = flagKey;
    }

    /**
     * Returns the key of the disabled feature flag.
     *
     * @return the flag key; never {@code null}
     */
    public String getFlagKey() {
        return flagKey;
    }
}
