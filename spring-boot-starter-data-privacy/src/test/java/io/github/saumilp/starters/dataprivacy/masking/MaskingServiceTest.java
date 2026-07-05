/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MaskingService}.
 */
class MaskingServiceTest {

    private final MaskingService service = new MaskingService();

    @Test
    void should_maskEmail() {
        assertThat(service.mask("john.doe@example.com", MaskStrategy.EMAIL)).isEqualTo("j***@example.com");
    }

    @Test
    void should_maskFullEmail_whenNoAtSymbol() {
        assertThat(service.mask("notanemail", MaskStrategy.EMAIL)).isEqualTo("**********");
    }

    @Test
    void should_maskCreditCard_keepingLastFour() {
        assertThat(service.mask("4111111111111111", MaskStrategy.CREDIT_CARD)).isEqualTo("************1111");
    }

    @Test
    void should_maskFull() {
        assertThat(service.mask("secret", MaskStrategy.FULL)).isEqualTo("******");
    }

    @Test
    void should_leaveUnchanged_forNoneStrategy() {
        assertThat(service.mask("value", MaskStrategy.NONE)).isEqualTo("value");
    }

    @Test
    void should_passThroughNullAndEmpty() {
        assertThat(service.mask(null, MaskStrategy.FULL)).isNull();
        assertThat(service.mask("", MaskStrategy.FULL)).isEmpty();
    }
}
