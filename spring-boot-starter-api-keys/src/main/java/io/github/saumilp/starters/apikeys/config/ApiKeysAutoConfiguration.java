/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.config;

import io.github.saumilp.starters.apikeys.hash.ApiKeyHasher;
import io.github.saumilp.starters.apikeys.service.ApiKeyService;
import io.github.saumilp.starters.apikeys.store.ApiKeyStore;
import io.github.saumilp.starters.apikeys.store.InMemoryApiKeyStore;
import io.github.saumilp.starters.apikeys.web.ApiKeyAuthFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Auto-configuration for the api-keys starter.
 *
 * <p>Registers an {@link ApiKeyHasher}, an in-memory {@link ApiKeyStore}, and the
 * {@link ApiKeyService}. In a servlet web application it also registers an {@link ApiKeyAuthFilter}
 * that enforces a valid key on the configured protected paths. Every bean is
 * {@link ConditionalOnMissingBean} so any component can be overridden.
 *
 * @author SaumilP
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(ApiKeysProperties.class)
@ConditionalOnProperty(prefix = "spring.api-keys", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ApiKeysAutoConfiguration {

    /** Creates the api-keys auto-configuration. */
    public ApiKeysAutoConfiguration() {
    }

    /**
     * Registers the key hasher.
     *
     * @param properties the starter properties; must not be {@code null}
     * @return an {@link ApiKeyHasher}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiKeyHasher apiKeyHasher(ApiKeysProperties properties) {
        return new ApiKeyHasher(properties.getHashAlgorithm());
    }

    /**
     * Registers the default in-memory key store.
     *
     * @return an {@link ApiKeyStore}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiKeyStore apiKeyStore() {
        return new InMemoryApiKeyStore();
    }

    /**
     * Registers the key service.
     *
     * @param store      the key store; must not be {@code null}
     * @param hasher     the key hasher; must not be {@code null}
     * @param properties the starter properties; must not be {@code null}
     * @return an {@link ApiKeyService}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiKeyService apiKeyService(ApiKeyStore store, ApiKeyHasher hasher,
                                       ApiKeysProperties properties) {
        return new ApiKeyService(store, hasher, properties.getKeyBytes(), properties.getPrefix());
    }

    /**
     * Servlet-only wiring: the enforcement filter.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletApiKeysConfiguration {

        /** Creates the servlet api-keys configuration. */
        ServletApiKeysConfiguration() {
        }

        /**
         * Registers the {@link ApiKeyAuthFilter} over the configured protected paths.
         *
         * @param service    the key service; must not be {@code null}
         * @param properties the starter properties; must not be {@code null}
         * @return the filter registration; never {@code null}
         */
        @Bean("apiKeyAuthFilterRegistration")
        @ConditionalOnMissingBean(name = "apiKeyAuthFilterRegistration")
        FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(
                ApiKeyService service, ApiKeysProperties properties) {
            ApiKeyAuthFilter filter = new ApiKeyAuthFilter(
                service, properties.getHeaderName(), properties.getProtectedPaths());
            FilterRegistrationBean<ApiKeyAuthFilter> registration =
                new FilterRegistrationBean<>(filter);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }
}
