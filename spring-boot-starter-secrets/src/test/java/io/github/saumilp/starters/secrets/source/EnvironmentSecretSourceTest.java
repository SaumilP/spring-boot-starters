/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.source;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentSecretSource}.
 */
class EnvironmentSecretSourceTest {

    @Test
    void should_returnValue_when_propertyPresent() {
        MockEnvironment environment = new MockEnvironment().withProperty("db.password", "s3cr3t");
        EnvironmentSecretSource source = new EnvironmentSecretSource(environment);

        assertThat(source.get("db.password")).contains("s3cr3t");
    }

    @Test
    void should_returnEmpty_when_propertyAbsent() {
        EnvironmentSecretSource source = new EnvironmentSecretSource(new MockEnvironment());

        assertThat(source.get("missing")).isEmpty();
    }
}
