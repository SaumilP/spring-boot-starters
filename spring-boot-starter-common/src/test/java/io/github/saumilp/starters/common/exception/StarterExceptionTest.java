/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StarterException} construction behaviour.
 */
class StarterExceptionTest {

    @Test
    void should_preserveMessage_when_constructedWithMessageOnly() {
        StarterException ex = new StarterException("test error");
        assertThat(ex.getMessage()).isEqualTo("test error");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void should_preserveMessageAndCause_when_constructedWithBoth() {
        RuntimeException cause = new RuntimeException("root cause");
        StarterException ex = new StarterException("wrapper", cause);
        assertThat(ex.getMessage()).isEqualTo("wrapper");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void should_wrapCauseMessage_when_constructedWithCauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        StarterException ex = new StarterException(cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void should_beInstanceOfStarterException_when_configurationExceptionThrown() {
        StarterConfigurationException ex = new StarterConfigurationException("bad config");
        assertThat(ex).isInstanceOf(StarterException.class);
        assertThat(ex.getMessage()).isEqualTo("bad config");
    }
}
