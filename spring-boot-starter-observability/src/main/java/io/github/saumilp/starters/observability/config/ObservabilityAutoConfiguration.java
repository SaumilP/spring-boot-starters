/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.config;

import io.github.saumilp.starters.observability.async.MdcTaskDecorator;
import io.github.saumilp.starters.observability.correlation.CorrelationIdFilter;
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
 * Auto-configuration for the observability starter.
 *
 * <p>Registers a {@link MdcTaskDecorator} for async MDC propagation and, in servlet web
 * applications, a {@link CorrelationIdFilter} at the highest filter precedence. Everything is
 * gated on {@code spring.observability.enabled} (default {@code true}) and each bean uses
 * {@link ConditionalOnMissingBean} so a consumer can override it.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnProperty(prefix = "spring.observability", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ObservabilityAutoConfiguration {

    /** Creates the observability auto-configuration. */
    public ObservabilityAutoConfiguration() {
    }

    /**
     * Provides a task decorator that propagates the MDC (correlation ID) across async boundaries.
     *
     * @return an {@link MdcTaskDecorator}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    /**
     * Servlet-only beans, activated when the correlation filter's servlet types are on the
     * classpath and the application is a servlet web application.
     *
     * @since 1.0.0
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletObservabilityConfiguration {

        /** Creates the servlet observability configuration. */
        ServletObservabilityConfiguration() {
        }

        /**
         * Registers the {@link CorrelationIdFilter} at the highest precedence so the correlation
         * ID is available to every downstream filter and handler.
         *
         * @param properties the observability properties; must not be {@code null}
         * @return the filter registration; never {@code null}
         */
        @Bean("correlationIdFilterRegistration")
        @ConditionalOnMissingBean(name = "correlationIdFilterRegistration")
        FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
                ObservabilityProperties properties) {
            FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>(new CorrelationIdFilter(properties.getCorrelation()));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }
}
