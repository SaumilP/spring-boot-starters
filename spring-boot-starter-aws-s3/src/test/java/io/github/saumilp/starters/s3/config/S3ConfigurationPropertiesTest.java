/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link S3ConfigurationProperties} default values.
 */
class S3ConfigurationPropertiesTest {

    private final S3ConfigurationProperties props = new S3ConfigurationProperties();

    @Test
    void should_haveDefaultRegion_when_notConfigured() {
        assertThat(props.getRegion()).isEqualTo("us-east-1");
    }

    @Test
    void should_havePathStyleAccessFalse_when_notConfigured() {
        assertThat(props.isPathStyleAccess()).isFalse();
    }

    @Test
    void should_haveDefaultPresignedUrlExpiry_when_notConfigured() {
        assertThat(props.getPresignedUrlExpiryMinutes()).isEqualTo(60L);
    }

    @Test
    void should_haveEnabledTrue_when_notConfigured() {
        assertThat(props.isEnabled()).isTrue();
    }
}
