/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FileBasedFeatureProvider} flag evaluation using the
 * in-memory test constructor.
 */
class FileBasedFeatureProviderTest {

    private FileBasedFeatureProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FileBasedFeatureProvider(Map.of(
            "enabled-flag",  true,
            "disabled-flag", false
        ));
    }

    @Test
    void should_returnTrue_when_flagIsEnabled() {
        boolean result = provider.getBooleanEvaluation("enabled-flag", false, null).getValue();
        assertThat(result).isTrue();
    }

    @Test
    void should_returnFalse_when_flagIsDisabled() {
        boolean result = provider.getBooleanEvaluation("disabled-flag", true, null).getValue();
        assertThat(result).isFalse();
    }

    @Test
    void should_returnDefault_when_flagIsUnknown() {
        boolean result = provider.getBooleanEvaluation("unknown-flag", true, null).getValue();
        assertThat(result).isTrue();
    }

    @Test
    void should_returnFalseDefault_when_unknownAndDefaultIsFalse() {
        boolean result = provider.getBooleanEvaluation("nonexistent", false, null).getValue();
        assertThat(result).isFalse();
    }

    @Test
    void should_reportCorrectProviderName() {
        assertThat(provider.getMetadata().getName()).isEqualTo("FileBasedFeatureProvider");
    }
}
