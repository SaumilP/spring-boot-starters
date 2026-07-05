/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import io.github.saumilp.starters.observability.config.ObservabilityProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CorrelationIdFilter}.
 */
class CorrelationIdFilterTest {

    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private ObservabilityProperties.Correlation config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        config = new ObservabilityProperties.Correlation();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_useProvidedHeader_when_present() throws Exception {
        request.addHeader(HEADER, "abc-123");
        AtomicReference<String> duringChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> duringChain.set(MDC.get(MDC_KEY));

        new CorrelationIdFilter(config).doFilter(request, response, chain);

        assertThat(duringChain.get()).isEqualTo("abc-123");
        assertThat(response.getHeader(HEADER)).isEqualTo("abc-123");
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void should_generateId_when_absentAndGenerationEnabled() throws Exception {
        config.setGenerateIfAbsent(true);
        AtomicReference<String> duringChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> duringChain.set(MDC.get(MDC_KEY));

        new CorrelationIdFilter(config).doFilter(request, response, chain);

        assertThat(duringChain.get()).isNotBlank();
        assertThat(response.getHeader(HEADER)).isEqualTo(duringChain.get());
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void should_notBind_when_absentAndGenerationDisabled() throws Exception {
        config.setGenerateIfAbsent(false);
        AtomicReference<String> duringChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> duringChain.set(MDC.get(MDC_KEY));

        new CorrelationIdFilter(config).doFilter(request, response, chain);

        assertThat(duringChain.get()).isNull();
        assertThat(response.getHeader(HEADER)).isNull();
    }

    @Test
    void should_honorCustomHeaderAndMdcKey() throws Exception {
        config.setHeaderName("X-Trace");
        config.setMdcKey("traceId");
        request.addHeader("X-Trace", "trace-9");
        AtomicReference<String> duringChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> duringChain.set(MDC.get("traceId"));

        new CorrelationIdFilter(config).doFilter(request, response, chain);

        assertThat(duringChain.get()).isEqualTo("trace-9");
        assertThat(response.getHeader("X-Trace")).isEqualTo("trace-9");
    }
}
