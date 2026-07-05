/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.config;

import io.github.saumilp.starters.llm.client.LlmClient;
import io.github.saumilp.starters.llm.client.RestClientLlmClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring Boot auto-configuration for the LLM client starter.
 *
 * <p>Registers the following beans when active:
 * <ul>
 *   <li>{@code llmRestClient} — a {@link RestClient} pre-configured with the endpoint
 *       base URL and HTTP timeouts from {@link LlmClientProperties}</li>
 *   <li>{@link LlmClient} — a {@link RestClientLlmClient} wired with the above client,
 *       properties, and an optional {@link MeterRegistry}</li>
 * </ul>
 *
 * <p>The entire configuration can be disabled via:
 * <pre>{@code spring.llm.enabled=false}</pre>
 *
 * <p>Consuming applications may replace either bean with their own implementation using
 * {@code @ConditionalOnMissingBean} — the auto-configuration honours this by applying
 * the same condition to its own beans.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.llm", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LlmClientProperties.class)
public class LlmClientAutoConfiguration {

    /** Creates the LLM client auto-configuration. */
    public LlmClientAutoConfiguration() {
    }

    /**
     * Creates a {@link RestClient} scoped to the LLM endpoint with configured timeouts.
     *
     * @param props the starter configuration; must not be {@code null}
     * @return a configured {@link RestClient}; never {@code null}
     */
    @Bean(name = "llmRestClient")
    @ConditionalOnMissingBean(name = "llmRestClient")
    public RestClient llmRestClient(LlmClientProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(props.getConnectTimeoutSeconds()));
        factory.setReadTimeout(Duration.ofSeconds(props.getReadTimeoutSeconds()));

        return RestClient.builder()
            .baseUrl(props.getBaseUrl())
            .requestFactory(factory)
            .build();
    }

    /**
     * Creates the primary {@link LlmClient} implementation.
     *
     * <p>The {@link MeterRegistry} is injected optionally — if Micrometer is not present
     * in the application context, metrics are silently skipped.
     *
     * @param llmRestClient  the LLM-scoped REST client; must not be {@code null}
     * @param props          the starter configuration; must not be {@code null}
     * @param meterRegistry  optional Micrometer registry; {@code null} if absent
     * @return a configured {@link RestClientLlmClient}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(LlmClient.class)
    public LlmClient llmClient(RestClient llmRestClient,
                                LlmClientProperties props,
                                @Autowired(required = false) MeterRegistry meterRegistry) {
        return new RestClientLlmClient(llmRestClient, props, meterRegistry);
    }
}
