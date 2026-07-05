/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.exception;

import io.github.saumilp.starters.common.exception.StarterException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link S3OperationException} construction and type hierarchy.
 */
class S3OperationExceptionTest {

    @Test
    void should_createWithMessage_when_messageProvided() {
        S3OperationException ex = new S3OperationException("upload failed");
        assertThat(ex.getMessage()).isEqualTo("upload failed");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void should_createWithCause_when_messageAndCauseProvided() {
        RuntimeException cause = new RuntimeException("network error");
        S3OperationException ex = new S3OperationException("download failed", cause);
        assertThat(ex.getMessage()).isEqualTo("download failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void should_beSubtypeOfStarterException() {
        assertThat(new S3OperationException("test"))
            .isInstanceOf(StarterException.class);
    }
}
