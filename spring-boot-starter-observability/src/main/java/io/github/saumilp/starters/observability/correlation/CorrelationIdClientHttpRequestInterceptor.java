/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Client interceptor that propagates the current correlation ID onto outbound HTTP calls.
 *
 * <p>When a correlation ID is bound to the current thread (see {@link CorrelationContext}) and the
 * outbound request does not already carry the header, the interceptor adds it so downstream
 * services observe the same ID.
 *
 * @since 1.0.0
 */
public class CorrelationIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final String headerName;

    /**
     * Constructs the interceptor.
     *
     * @param headerName the correlation header name to propagate; must not be {@code null} or blank
     */
    public CorrelationIdClientHttpRequestInterceptor(String headerName) {
        this.headerName = headerName;
    }

    /**
     * {@inheritDoc}
     *
     * @param request   the outbound request; must not be {@code null}
     * @param body      the request body
     * @param execution the request execution to delegate to; must not be {@code null}
     * @return the response from the executed request; never {@code null}
     * @throws IOException if the execution fails
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (!request.getHeaders().containsHeader(headerName)) {
            CorrelationContext.getCorrelationId()
                .ifPresent(id -> request.getHeaders().add(headerName, id));
        }
        return execution.execute(request, body);
    }
}
