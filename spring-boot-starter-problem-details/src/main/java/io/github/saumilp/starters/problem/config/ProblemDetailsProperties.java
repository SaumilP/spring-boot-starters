/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.problem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the problem-details starter, bound from the
 * {@code spring.problem-details.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   problem-details:
 *     enabled: true
 *     base-type-uri: https://errors.example.com
 *     include-stack-trace: false
 *     include-correlation-id: true
 *     correlation-mdc-key: correlationId
 *     field-errors-key: errors
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.problem-details")
public class ProblemDetailsProperties {

    /** Whether the global RFC 7807 exception handler is enabled. */
    private boolean enabled = true;

    /**
     * Base URI used to build the {@code type} member. When left as {@code about:blank} the type is
     * omitted per RFC 7807; otherwise the problem code is appended, e.g. {@code <base>/validation-error}.
     */
    private String baseTypeUri = "about:blank";

    /** Whether to include a stack trace on {@code 500} responses (never enable in production). */
    private boolean includeStackTrace = false;

    /** Whether to include the current correlation ID (read from the MDC) as an extension member. */
    private boolean includeCorrelationId = true;

    /** MDC key read for the correlation ID; aligns with the observability starter's default. */
    private String correlationMdcKey = "correlationId";

    /** Extension member name holding the list of per-field validation errors. */
    private String fieldErrorsKey = "errors";

    /** Creates an instance with default values. */
    public ProblemDetailsProperties() {
    }

    /** {@return whether the handler is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the handler is enabled.
     *
     * @param enabled {@code false} to disable the global exception handler
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the base URI used to build the problem {@code type} member} */
    public String getBaseTypeUri() {
        return baseTypeUri;
    }

    /**
     * Sets the base URI used to build the problem {@code type} member.
     *
     * @param baseTypeUri the base type URI; must not be {@code null}
     */
    public void setBaseTypeUri(String baseTypeUri) {
        this.baseTypeUri = baseTypeUri;
    }

    /** {@return whether a stack trace is included on server errors} */
    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    /**
     * Sets whether a stack trace is included on server errors.
     *
     * @param includeStackTrace {@code true} to include the stack trace (development only)
     */
    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }

    /** {@return whether the correlation ID is added as an extension member} */
    public boolean isIncludeCorrelationId() {
        return includeCorrelationId;
    }

    /**
     * Sets whether the correlation ID is added as an extension member.
     *
     * @param includeCorrelationId {@code true} to include the correlation ID when present
     */
    public void setIncludeCorrelationId(boolean includeCorrelationId) {
        this.includeCorrelationId = includeCorrelationId;
    }

    /** {@return the MDC key read for the correlation ID} */
    public String getCorrelationMdcKey() {
        return correlationMdcKey;
    }

    /**
     * Sets the MDC key read for the correlation ID.
     *
     * @param correlationMdcKey the MDC key; must not be {@code null} or blank
     */
    public void setCorrelationMdcKey(String correlationMdcKey) {
        this.correlationMdcKey = correlationMdcKey;
    }

    /** {@return the extension member name holding field validation errors} */
    public String getFieldErrorsKey() {
        return fieldErrorsKey;
    }

    /**
     * Sets the extension member name holding field validation errors.
     *
     * @param fieldErrorsKey the extension member name; must not be {@code null} or blank
     */
    public void setFieldErrorsKey(String fieldErrorsKey) {
        this.fieldErrorsKey = fieldErrorsKey;
    }
}
