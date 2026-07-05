/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the LLM client starter, bound from the
 * {@code spring.llm.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   llm:
 *     enabled: true
 *     base-url: https://api.openai.com
 *     api-key: ${OPENAI_API_KEY}
 *     default-model: gpt-4o-mini
 *     max-retries: 3
 *     connect-timeout-seconds: 10
 *     read-timeout-seconds: 60
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.llm")
public class LlmClientProperties {

    /** Creates an instance with default values. */
    public LlmClientProperties() {
    }

    /** Whether the LLM client is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /**
     * Base URL of the OpenAI-compatible endpoint. Must not include a trailing slash.
     * Defaults to the official OpenAI API endpoint.
     */
    private String baseUrl = "https://api.openai.com";

    /**
     * API key sent in the {@code Authorization: Bearer} header. When left empty the
     * client sends an empty bearer token; set via the {@code OPENAI_API_KEY} environment
     * variable in production.
     */
    private String apiKey = "";

    /**
     * The model identifier used when the {@link io.github.saumilp.starters.llm.client.LlmClient#ask}
     * convenience method is called, or when a request explicitly specifies {@code "default"}.
     * Defaults to {@code "gpt-4o-mini"}.
     */
    private String defaultModel = "gpt-4o-mini";

    /**
     * Maximum number of retry attempts on transient {@code RestClientException} failures.
     * A value of {@code 0} disables retries. Defaults to {@code 3}.
     */
    private int maxRetries = 3;

    /**
     * HTTP connection timeout in seconds. Defaults to {@code 10}.
     */
    private int connectTimeoutSeconds = 10;

    /**
     * HTTP read timeout in seconds — how long to wait for the LLM to start streaming a
     * response. Defaults to {@code 60} to accommodate slow model inference.
     */
    private int readTimeoutSeconds = 60;

    // --- getters / setters ---

    /**
     * Returns whether the LLM client is globally enabled.
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }
    /**
     * Sets whether the LLM client is enabled.
     *
     * @param enabled {@code false} to disable the auto-configured client
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the base URL of the LLM endpoint.
     * @return the base URL; never {@code null}
     */
    public String getBaseUrl() { return baseUrl; }
    /**
     * Sets the base URL of the LLM endpoint.
     *
     * @param baseUrl the endpoint base URL; must not be {@code null}
     */
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    /**
     * Returns the API key used for authentication.
     * @return the API key; may be empty
     */
    public String getApiKey() { return apiKey; }
    /**
     * Sets the API key used for authentication.
     *
     * @param apiKey the API key; must not be {@code null}
     */
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    /**
     * Returns the default model identifier.
     * @return the model name; never {@code null}
     */
    public String getDefaultModel() { return defaultModel; }
    /**
     * Sets the default model identifier.
     *
     * @param defaultModel the model name; must not be blank
     */
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }

    /**
     * Returns the maximum number of retry attempts.
     * @return the retry count; always non-negative
     */
    public int getMaxRetries() { return maxRetries; }
    /**
     * Sets the maximum number of retry attempts.
     *
     * @param maxRetries the retry count; must be non-negative
     */
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    /**
     * Returns the HTTP connection timeout in seconds.
     * @return the timeout; always positive
     */
    public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
    /**
     * Sets the HTTP connection timeout in seconds.
     *
     * @param connectTimeoutSeconds the connection timeout; must be positive
     */
    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    /**
     * Returns the HTTP read timeout in seconds.
     * @return the timeout; always positive
     */
    public int getReadTimeoutSeconds() { return readTimeoutSeconds; }
    /**
     * Sets the HTTP read timeout in seconds.
     *
     * @param readTimeoutSeconds the read timeout; must be positive
     */
    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }
}
