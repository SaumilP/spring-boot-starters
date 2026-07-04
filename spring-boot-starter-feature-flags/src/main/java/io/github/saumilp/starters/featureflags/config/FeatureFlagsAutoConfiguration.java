/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.featureflags.config;

import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import io.github.saumilp.starters.featureflags.aspect.FeatureEnabledAspect;
import io.github.saumilp.starters.featureflags.provider.FileBasedFeatureProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

/**
 * Spring Boot auto-configuration for the feature-flags starter.
 *
 * <p>Registers the following beans when active:
 * <ul>
 *   <li>{@link FileBasedFeatureProvider} — loads flags from a YAML file; active when no
 *       other {@link FeatureProvider} bean is declared</li>
 *   <li>{@link OpenFeatureAPI} — the OpenFeature singleton, initialised with the active
 *       provider; active when no other {@code OpenFeatureAPI} bean is declared</li>
 *   <li>{@link FeatureEnabledAspect} — the AOP interceptor that enforces
 *       {@link io.github.saumilp.starters.featureflags.annotation.FeatureEnabled}
 *       annotations</li>
 * </ul>
 *
 * <p>The entire configuration can be disabled via:
 * <pre>{@code spring.feature-flags.enabled=false}</pre>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.feature-flags", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FeatureFlagProperties.class)
public class FeatureFlagsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagsAutoConfiguration.class);

    /**
     * Registers the {@link FileBasedFeatureProvider} as the default {@link FeatureProvider}.
     *
     * <p>Consuming applications can replace this by declaring their own {@link FeatureProvider}
     * bean (e.g., an Unleash-backed provider).
     *
     * @param resourceLoader the Spring resource loader for resolving the flag file path;
     *                       must not be {@code null}
     * @param props          the starter configuration properties; must not be {@code null}
     * @return a configured {@link FileBasedFeatureProvider}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(FeatureProvider.class)
    public FileBasedFeatureProvider fileBasedFeatureProvider(ResourceLoader resourceLoader,
                                                              FeatureFlagProperties props) {
        return new FileBasedFeatureProvider(resourceLoader, props.getFilePath());
    }

    /**
     * Registers and initialises the {@link OpenFeatureAPI} singleton with the active provider.
     *
     * <p>Uses {@code setProviderAndWait} to ensure the provider is fully initialised before
     * the application context completes startup. Any initialisation failure is logged but
     * does not prevent the context from starting.
     *
     * @param provider the active feature provider; must not be {@code null}
     * @return the initialised {@link OpenFeatureAPI} singleton; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(OpenFeatureAPI.class)
    public OpenFeatureAPI openFeatureAPI(FeatureProvider provider) {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        try {
            api.setProviderAndWait(provider);
            log.info("OpenFeature provider '{}' registered successfully.", provider.getMetadata().getName());
        } catch (Exception ex) {
            log.error("Failed to initialise OpenFeature provider '{}': {}",
                provider.getMetadata().getName(), ex.getMessage(), ex);
        }
        return api;
    }

    /**
     * Registers the {@link FeatureEnabledAspect} AOP interceptor.
     *
     * @param openFeatureAPI the initialised OpenFeature API; must not be {@code null}
     * @return a configured {@link FeatureEnabledAspect}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(FeatureEnabledAspect.class)
    public FeatureEnabledAspect featureEnabledAspect(OpenFeatureAPI openFeatureAPI) {
        return new FeatureEnabledAspect(openFeatureAPI);
    }
}
