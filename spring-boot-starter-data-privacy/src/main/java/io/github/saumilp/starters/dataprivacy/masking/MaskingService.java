/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.masking;

/**
 * Masks sensitive string values for safe display or logging.
 *
 * <p>Stateless and thread-safe. Callers pick a {@link MaskStrategy} appropriate to the data type;
 * {@code null} and empty inputs are returned unchanged.
 *
 * @since 1.0.0
 */
public class MaskingService {

    /** Creates a new masking service. */
    public MaskingService() {
    }

    /**
     * Masks the given value using the requested strategy.
     *
     * @param value    the value to mask; {@code null} or empty is returned unchanged
     * @param strategy the strategy to apply; must not be {@code null}
     * @return the masked value
     */
    public String mask(String value, MaskStrategy strategy) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return switch (strategy) {
            case EMAIL -> maskEmail(value);
            case CREDIT_CARD -> maskCreditCard(value);
            case FULL -> "*".repeat(value.length());
            case NONE -> value;
        };
    }

    private String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0) {
            return "*".repeat(value.length());
        }
        return value.charAt(0) + "***" + value.substring(at);
    }

    private String maskCreditCard(String value) {
        int visible = 4;
        if (value.length() <= visible) {
            return "*".repeat(value.length());
        }
        String suffix = value.substring(value.length() - visible);
        return "*".repeat(value.length() - visible) + suffix;
    }
}
