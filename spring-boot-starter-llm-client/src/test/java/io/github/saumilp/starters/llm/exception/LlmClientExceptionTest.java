/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.exception;

import io.github.saumilp.starters.common.exception.StarterException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LlmClientException} construction.
 */
class LlmClientExceptionTest {

    @Test
    void should_setMessage_when_constructedWithMessage() {
        LlmClientException ex = new LlmClientException("LLM unavailable");
        assertThat(ex.getMessage()).isEqualTo("LLM unavailable");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void should_setMessageAndCause_when_constructedWithBoth() {
        RuntimeException cause = new RuntimeException("timeout");
        LlmClientException ex = new LlmClientException("Request failed", cause);
        assertThat(ex.getMessage()).isEqualTo("Request failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void should_wrapCause_when_constructedWithCauseOnly() {
        RuntimeException cause = new RuntimeException("connection refused");
        LlmClientException ex = new LlmClientException(cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void should_beSubtypeOfStarterException() {
        assertThat(new LlmClientException("test"))
            .isInstanceOf(StarterException.class);
    }
}
