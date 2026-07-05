/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.provider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An OpenFeature {@link FeatureProvider} that loads feature flag definitions from a
 * flat YAML file at application startup.
 *
 * <p>The YAML file must contain a flat map of flag keys to boolean values:
 * <pre>{@code
 * new-checkout: true
 * beta-dashboard: false
 * dark-mode: true
 * }</pre>
 *
 * <p>The file path is configurable via {@code spring.feature-flags.file-path}
 * (default: {@code classpath:feature-flags.yml}). If the file is absent the provider
 * logs a warning and returns default values for all flag evaluations — it never throws.
 *
 * <p>Flag values are loaded once at construction time. To pick up changes, the application
 * context must be restarted, or the consumer must supply a custom {@link FeatureProvider}
 * implementation with live-reload support.
 *
 * @since 1.0.0
 */
public class FileBasedFeatureProvider implements FeatureProvider {

    private static final Logger log = LoggerFactory.getLogger(FileBasedFeatureProvider.class);

    private final Map<String, Boolean> flags;

    /**
     * Constructs a {@code FileBasedFeatureProvider} by loading flags from the resource
     * at the given path.
     *
     * @param resourceLoader the Spring resource loader used to resolve the path;
     *                       must not be {@code null}
     * @param filePath       the resource path to the YAML flag file
     *                       (e.g. {@code "classpath:feature-flags.yml"});
     *                       must not be {@code null}
     */
    public FileBasedFeatureProvider(ResourceLoader resourceLoader, String filePath) {
        this.flags = loadFlags(resourceLoader, filePath);
    }

    /**
     * Package-private constructor for unit testing — accepts a pre-populated flag map
     * directly, bypassing file I/O.
     *
     * @param flags the flag map to use; must not be {@code null}
     */
    FileBasedFeatureProvider(Map<String, Boolean> flags) {
        this.flags = Collections.unmodifiableMap(new HashMap<>(flags));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "file-based"}
     */
    @Override
    public Metadata getMetadata() {
        return () -> "FileBasedFeatureProvider";
    }

    /**
     * Evaluates a boolean feature flag by looking it up in the loaded flag map.
     *
     * @param flagKey      the flag key to evaluate; must not be {@code null}
     * @param defaultValue the value to return when the flag is not defined;
     *                     must not be {@code null}
     * @param ctx          the evaluation context; may be {@code null}
     * @return a {@link ProviderEvaluation} carrying the resolved flag value
     */
    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String flagKey, Boolean defaultValue,
                                                             EvaluationContext ctx) {
        return ProviderEvaluation.<Boolean>builder()
            .value(flags.getOrDefault(flagKey, defaultValue))
            .build();
    }

    /**
     * Not supported by this provider — returns the default value unchanged.
     *
     * @param flagKey      the flag key
     * @param defaultValue the default value returned unconditionally
     * @param ctx          the evaluation context
     * @return a {@link ProviderEvaluation} wrapping {@code defaultValue}
     */
    @Override
    public ProviderEvaluation<String> getStringEvaluation(String flagKey, String defaultValue,
                                                           EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder().value(defaultValue).build();
    }

    /**
     * Not supported by this provider — returns the default value unchanged.
     *
     * @param flagKey      the flag key
     * @param defaultValue the default value returned unconditionally
     * @param ctx          the evaluation context
     * @return a {@link ProviderEvaluation} wrapping {@code defaultValue}
     */
    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String flagKey, Integer defaultValue,
                                                             EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder().value(defaultValue).build();
    }

    /**
     * Not supported by this provider — returns the default value unchanged.
     *
     * @param flagKey      the flag key
     * @param defaultValue the default value returned unconditionally
     * @param ctx          the evaluation context
     * @return a {@link ProviderEvaluation} wrapping {@code defaultValue}
     */
    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String flagKey, Double defaultValue,
                                                           EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder().value(defaultValue).build();
    }

    /**
     * Not supported by this provider — returns the default value unchanged.
     *
     * @param flagKey      the flag key
     * @param defaultValue the default value returned unconditionally
     * @param ctx          the evaluation context
     * @return a {@link ProviderEvaluation} wrapping {@code defaultValue}
     */
    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String flagKey, Value defaultValue,
                                                          EvaluationContext ctx) {
        return ProviderEvaluation.<Value>builder().value(defaultValue).build();
    }

    private Map<String, Boolean> loadFlags(ResourceLoader resourceLoader, String filePath) {
        Map<String, Boolean> result = new HashMap<>();
        try {
            Resource resource = resourceLoader.getResource(filePath);
            if (!resource.exists()) {
                log.warn("Feature flag file '{}' not found — all flags will use default values.", filePath);
                return result;
            }
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResources(resource);
            Properties props = yaml.getObject();
            if (props != null) {
                props.forEach((k, v) -> {
                    if (k != null && v != null) {
                        result.put(k.toString(), Boolean.parseBoolean(v.toString()));
                    }
                });
            }
            log.info("Loaded {} feature flag(s) from '{}'.", result.size(), filePath);
        } catch (Exception ex) {
            log.warn("Failed to load feature flags from '{}': {} — using defaults.", filePath, ex.getMessage());
        }
        return Collections.unmodifiableMap(result);
    }
}
