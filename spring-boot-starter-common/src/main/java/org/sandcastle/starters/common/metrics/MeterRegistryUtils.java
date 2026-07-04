/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.common.metrics;

/**
 * Shared constants for Micrometer metric tag names used across all starters.
 *
 * <p>Using shared constants prevents tag name inconsistencies when multiple starters contribute
 * metrics to the same monitoring backend. Dashboard queries and alert rules can rely on these
 * names being stable across starter versions.
 *
 * <p>All constants are {@code public static final String} values. This class is not
 * instantiable.
 *
 * @since 1.0.0
 */
public final class MeterRegistryUtils {

    /** Tag name identifying the name of the operation that was measured. */
    public static final String TAG_OPERATION = "operation";

    /** Tag name indicating the outcome of an operation: {@code "success"} or {@code "error"}. */
    public static final String TAG_STATUS = "status";

    /** Tag value for a successfully completed operation. */
    public static final String STATUS_SUCCESS = "success";

    /** Tag value for an operation that ended with an exception or error response. */
    public static final String STATUS_ERROR = "error";

    /** Tag name identifying the name of the starter or component emitting the metric. */
    public static final String TAG_COMPONENT = "component";

    private MeterRegistryUtils() {
        throw new UnsupportedOperationException("MeterRegistryUtils is a constants class and cannot be instantiated.");
    }
}
