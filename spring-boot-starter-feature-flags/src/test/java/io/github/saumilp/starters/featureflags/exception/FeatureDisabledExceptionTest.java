/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.exception;

import io.github.saumilp.starters.common.exception.StarterException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FeatureDisabledException} construction and field exposure.
 */
class FeatureDisabledExceptionTest {

    @Test
    void should_exposeFlagKey_when_constructed() {
        FeatureDisabledException ex = new FeatureDisabledException("new-checkout");
        assertThat(ex.getFlagKey()).isEqualTo("new-checkout");
    }

    @Test
    void should_inclueFlagKeyInMessage_when_constructed() {
        FeatureDisabledException ex = new FeatureDisabledException("beta-dashboard");
        assertThat(ex.getMessage()).contains("beta-dashboard");
    }

    @Test
    void should_beSubtypeOfStarterException() {
        assertThat(new FeatureDisabledException("flag")).isInstanceOf(StarterException.class);
    }
}
