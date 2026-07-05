/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.actor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FallbackActorResolver}.
 */
class FallbackActorResolverTest {

    @Test
    void should_returnAnonymous_always() {
        assertThat(new FallbackActorResolver().resolve()).isEqualTo("anonymous");
    }
}
