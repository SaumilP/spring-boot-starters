/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.health;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for constructing the detail map passed to Spring Boot Actuator
 * {@link org.springframework.boot.health.contributor.Health} instances.
 *
 * <p>Provides a consistent, null-safe way to build health detail maps across all starter
 * health indicators without scattering {@code Map.of()} calls throughout the codebase.
 *
 * <p>Example:
 * <pre>{@code
 * Map<String, Object> details = HealthDetails.builder()
 *     .add("host", props.getHost())
 *     .add("port", props.getPort())
 *     .add("bucket", props.getBucket())
 *     .build();
 * return Health.up().withDetails(details).build();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class HealthDetails {

    private final Map<String, Object> details = new LinkedHashMap<>();

    private HealthDetails() {}

    /**
     * Creates a new empty {@code HealthDetails} builder.
     *
     * @return a fresh builder instance; never {@code null}
     */
    public static HealthDetails builder() {
        return new HealthDetails();
    }

    /**
     * Adds a key-value pair to the detail map.
     *
     * <p>If {@code value} is {@code null}, the entry is omitted rather than stored with a
     * {@code null} value, preventing {@code NullPointerException} in serialisers that cannot
     * handle null map values.
     *
     * @param key   the detail key; must not be {@code null} or blank
     * @param value the detail value; {@code null} values are silently skipped
     * @return this builder for method chaining; never {@code null}
     */
    public HealthDetails add(String key, Object value) {
        if (value != null) {
            details.put(key, value);
        }
        return this;
    }

    /**
     * Returns the accumulated detail map.
     *
     * <p>Insertion order is preserved. A defensive copy is taken so the returned map is an
     * unmodifiable, order-preserving snapshot that is unaffected by further mutations of this
     * builder. ({@link Map#copyOf(Map)} is deliberately avoided here because it makes no
     * iteration-order guarantee.)
     *
     * @return an unmodifiable, insertion-ordered snapshot of the detail entries collected so
     *         far; never {@code null}
     */
    public Map<String, Object> build() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }
}
