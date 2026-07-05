/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.masking;

/**
 * The masking strategy applied by {@link MaskingService}.
 *
 * @since 1.0.0
 */
public enum MaskStrategy {

    /** Mask the local part of an email, keeping the first character and the domain (e.g. {@code j***@x.com}). */
    EMAIL,

    /** Mask all but the last four digits of a card-like number (e.g. {@code ************1111}). */
    CREDIT_CARD,

    /** Replace every character with {@code *}. */
    FULL,

    /** Leave the value unchanged. */
    NONE
}
