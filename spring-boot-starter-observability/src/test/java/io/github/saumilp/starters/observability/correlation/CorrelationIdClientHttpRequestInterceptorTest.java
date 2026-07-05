/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CorrelationIdClientHttpRequestInterceptor}.
 */
class CorrelationIdClientHttpRequestInterceptorTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_addHeader_when_correlationIdBoundAndHeaderAbsent() throws Exception {
        MDC.put(CorrelationContext.mdcKey(), "id-42");
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(mock(ClientHttpResponse.class));

        new CorrelationIdClientHttpRequestInterceptor("X-Correlation-Id")
            .intercept(request, new byte[0], execution);

        assertThat(headers.getFirst("X-Correlation-Id")).isEqualTo("id-42");
    }

    @Test
    void should_notOverrideExistingHeader() throws Exception {
        MDC.put(CorrelationContext.mdcKey(), "id-42");
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Correlation-Id", "preset");
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(mock(ClientHttpResponse.class));

        new CorrelationIdClientHttpRequestInterceptor("X-Correlation-Id")
            .intercept(request, new byte[0], execution);

        assertThat(headers.get("X-Correlation-Id")).containsExactly("preset");
    }

    @Test
    void should_notAddHeader_when_noCorrelationIdBound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(headers);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(mock(ClientHttpResponse.class));

        new CorrelationIdClientHttpRequestInterceptor("X-Correlation-Id")
            .intercept(request, new byte[0], execution);

        assertThat(headers.containsHeader("X-Correlation-Id")).isFalse();
    }
}
