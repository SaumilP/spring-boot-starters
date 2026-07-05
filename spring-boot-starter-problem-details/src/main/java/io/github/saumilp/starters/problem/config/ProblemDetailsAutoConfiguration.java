/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.problem.config;

import io.github.saumilp.starters.problem.web.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ProblemDetail;

/**
 * Auto-configuration for the problem-details starter.
 *
 * <p>Registers a {@link GlobalExceptionHandler} in servlet web applications when
 * {@code spring.problem-details.enabled} is {@code true} (the default). The handler is annotated
 * {@link ConditionalOnMissingBean} so a consumer can supply their own advice.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ProblemDetail.class)
@EnableConfigurationProperties(ProblemDetailsProperties.class)
@ConditionalOnProperty(prefix = "spring.problem-details", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ProblemDetailsAutoConfiguration {

    /** Creates the problem-details auto-configuration. */
    public ProblemDetailsAutoConfiguration() {
    }

    /**
     * Registers the global RFC 7807 exception handler.
     *
     * @param properties the problem-details configuration; must not be {@code null}
     * @return a {@link GlobalExceptionHandler}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ProblemDetailsProperties properties) {
        return new GlobalExceptionHandler(properties);
    }
}
