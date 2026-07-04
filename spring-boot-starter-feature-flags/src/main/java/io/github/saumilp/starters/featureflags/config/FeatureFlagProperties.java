/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the feature-flags starter, bound from the
 * {@code spring.feature-flags.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   feature-flags:
 *     enabled: true
 *     file-path: classpath:feature-flags.yml
 *     provider: file
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.feature-flags")
public class FeatureFlagProperties {

    /** Whether feature flag evaluation is globally enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /**
     * Resource path to the YAML file containing flag definitions.
     * Supports Spring resource prefixes ({@code classpath:}, {@code file:}).
     * Defaults to {@code "classpath:feature-flags.yml"}.
     */
    private String filePath = "classpath:feature-flags.yml";

    /**
     * The feature provider implementation to use. Supported values:
     * <ul>
     *   <li>{@code "file"} — the built-in {@link
     *       io.github.saumilp.starters.featureflags.provider.FileBasedFeatureProvider}
     *       (default)</li>
     *   <li>{@code "unleash"} — use the Unleash SDK; requires adding the Unleash client
     *       dependency and registering a custom {@link dev.openfeature.sdk.FeatureProvider}
     *       bean</li>
     * </ul>
     */
    private String provider = "file";

    /**
     * Returns whether feature flag evaluation is enabled.
     *
     * @return {@code true} if enabled; {@code false} to disable entirely
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets the global enable flag.
     *
     * @param enabled {@code false} disables all flag evaluation; the AOP aspect will not
     *                be registered
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the resource path to the YAML flag-definition file.
     *
     * @return the file path; never {@code null}
     */
    public String getFilePath() { return filePath; }

    /**
     * Sets the resource path for the flag-definition YAML file.
     *
     * @param filePath the Spring resource path; must not be {@code null}
     */
    public void setFilePath(String filePath) { this.filePath = filePath; }

    /**
     * Returns the configured provider name.
     *
     * @return the provider identifier ({@code "file"} or {@code "unleash"});
     *         never {@code null}
     */
    public String getProvider() { return provider; }

    /**
     * Sets the provider implementation to use.
     *
     * @param provider the provider name; must not be {@code null}
     */
    public void setProvider(String provider) { this.provider = provider; }
}
