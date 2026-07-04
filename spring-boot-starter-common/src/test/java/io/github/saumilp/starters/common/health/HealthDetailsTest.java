/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HealthDetails} builder behaviour.
 */
class HealthDetailsTest {

    @Test
    void should_includeEntry_when_nonNullValueAdded() {
        Map<String, Object> details = HealthDetails.builder()
            .add("host", "localhost")
            .add("port", 6379)
            .build();

        assertThat(details).containsEntry("host", "localhost")
                           .containsEntry("port", 6379);
    }

    @Test
    void should_omitEntry_when_nullValueAdded() {
        Map<String, Object> details = HealthDetails.builder()
            .add("host", "localhost")
            .add("nullKey", null)
            .build();

        assertThat(details).containsKey("host")
                           .doesNotContainKey("nullKey");
    }

    @Test
    void should_returnEmptyMap_when_noEntriesAdded() {
        Map<String, Object> details = HealthDetails.builder().build();
        assertThat(details).isEmpty();
    }

    @Test
    void should_preserveInsertionOrder_when_multipleEntriesAdded() {
        Map<String, Object> details = HealthDetails.builder()
            .add("a", 1)
            .add("b", 2)
            .add("c", 3)
            .build();

        assertThat(details.keySet()).containsExactly("a", "b", "c");
    }
}
