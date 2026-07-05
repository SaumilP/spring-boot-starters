/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.config;

import io.github.saumilp.starters.multitenancy.hibernate.SchemaMultiTenantConnectionProvider;
import io.github.saumilp.starters.multitenancy.hibernate.TenantIdentifierResolver;
import io.github.saumilp.starters.multitenancy.resolver.HeaderTenantResolver;
import io.github.saumilp.starters.multitenancy.resolver.SubdomainTenantResolver;
import io.github.saumilp.starters.multitenancy.resolver.TenantResolver;
import io.github.saumilp.starters.multitenancy.web.TenantResolutionFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * Spring Boot auto-configuration for the multitenancy starter.
 *
 * <p>Registers the following beans depending on the application classpath and configuration:
 * <ul>
 *   <li>{@link HeaderTenantResolver} or {@link SubdomainTenantResolver} — based on
 *       {@code spring.multitenancy.resolver-type}</li>
 *   <li>{@link TenantResolutionFilter} wrapped in a {@link FilterRegistrationBean} at
 *       {@code HIGHEST_PRECEDENCE + 5}</li>
 *   <li>{@link SchemaMultiTenantConnectionProvider} — when Hibernate and a DataSource are
 *       present</li>
 *   <li>{@link TenantIdentifierResolver} — when Hibernate is present</li>
 * </ul>
 *
 * <p>All beans use {@code @ConditionalOnMissingBean} so consuming applications can replace
 * any individual component. The entire auto-configuration can be disabled via:
 * <pre>{@code spring.multitenancy.enabled=false}</pre>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.multitenancy", name = "enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MultitenancyProperties.class)
public class MultitenancyAutoConfiguration {

    /** Creates the multitenancy auto-configuration. */
    public MultitenancyAutoConfiguration() {
    }

    /**
     * Registers the {@link HeaderTenantResolver} when
     * {@code spring.multitenancy.resolver-type=HEADER} (the default).
     *
     * @param props the multitenancy configuration; must not be {@code null}
     * @return a {@link HeaderTenantResolver} reading from the configured header name
     */
    @Bean
    @ConditionalOnMissingBean(TenantResolver.class)
    @ConditionalOnProperty(prefix = "spring.multitenancy", name = "resolver-type",
        havingValue = "HEADER", matchIfMissing = true)
    public TenantResolver headerTenantResolver(MultitenancyProperties props) {
        return new HeaderTenantResolver(props.getTenantHeaderName());
    }

    /**
     * Registers the {@link SubdomainTenantResolver} when
     * {@code spring.multitenancy.resolver-type=SUBDOMAIN}.
     *
     * @return a {@link SubdomainTenantResolver}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(TenantResolver.class)
    @ConditionalOnProperty(prefix = "spring.multitenancy", name = "resolver-type",
        havingValue = "SUBDOMAIN")
    public TenantResolver subdomainTenantResolver() {
        return new SubdomainTenantResolver();
    }

    /**
     * Registers the {@link TenantResolutionFilter} at {@code HIGHEST_PRECEDENCE + 5}.
     *
     * @param resolver the active tenant resolver; must not be {@code null}
     * @param props    the multitenancy configuration; must not be {@code null}
     * @return a {@link FilterRegistrationBean} wrapping the filter; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(TenantResolutionFilter.class)
    public FilterRegistrationBean<TenantResolutionFilter> tenantResolutionFilter(
        TenantResolver resolver, MultitenancyProperties props) {

        FilterRegistrationBean<TenantResolutionFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TenantResolutionFilter(resolver, props));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        reg.setName("tenantResolutionFilter");
        return reg;
    }

    /**
     * Registers the Hibernate {@link SchemaMultiTenantConnectionProvider} when
     * {@code hibernate-core} and a {@link DataSource} are present.
     *
     * @param dataSource the primary data source; must not be {@code null}
     * @return a configured {@link SchemaMultiTenantConnectionProvider}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(SchemaMultiTenantConnectionProvider.class)
    @ConditionalOnClass(name = "org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider")
    @ConditionalOnBean(DataSource.class)
    public SchemaMultiTenantConnectionProvider schemaMultiTenantConnectionProvider(
        DataSource dataSource) {
        return new SchemaMultiTenantConnectionProvider(dataSource);
    }

    /**
     * Registers the Hibernate {@link TenantIdentifierResolver} when {@code hibernate-core}
     * is present.
     *
     * @return a {@link TenantIdentifierResolver}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(TenantIdentifierResolver.class)
    @ConditionalOnClass(name = "org.hibernate.context.spi.CurrentTenantIdentifierResolver")
    public TenantIdentifierResolver tenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }
}
