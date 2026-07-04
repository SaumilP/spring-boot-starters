/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.ratelimit.exception;

import org.junit.jupiter.api.Test;
import org.sandcastle.starters.common.exception.StarterException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RateLimitExceededException} construction and field access.
 */
class RateLimitExceededExceptionTest {

    @Test
    void should_exposeAllFields_when_constructed() {
        RateLimitExceededException ex = new RateLimitExceededException("rl:user:search", 60, 60L);
        assertThat(ex.getLimitKey()).isEqualTo("rl:user:search");
        assertThat(ex.getMaxRequests()).isEqualTo(60);
        assertThat(ex.getWindowSeconds()).isEqualTo(60L);
        assertThat(ex.getMessage()).contains("rl:user:search").contains("60");
    }

    @Test
    void should_beSubtypeOfStarterException() {
        assertThat(new RateLimitExceededException("k", 1, 1))
            .isInstanceOf(StarterException.class);
    }

    @Test
    void should_includeWindowInMessage_when_constructed() {
        RateLimitExceededException ex = new RateLimitExceededException("rl:ip:endpoint", 100, 3600L);
        assertThat(ex.getMessage()).contains("3600");
        assertThat(ex.getMessage()).contains("100");
    }
}
