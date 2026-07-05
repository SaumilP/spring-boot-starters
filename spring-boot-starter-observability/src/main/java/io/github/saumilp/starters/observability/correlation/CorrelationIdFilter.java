/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import io.github.saumilp.starters.observability.config.ObservabilityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that establishes a correlation ID for every request.
 *
 * <p>On each request the filter reads the configured correlation header; if present its value is
 * used, otherwise (when generation is enabled) a random UUID is created. The ID is placed in the
 * SLF4J {@link MDC} for the duration of the request, echoed back on the response header, and
 * cleared in a {@code finally} block so it never leaks to a pooled thread.
 *
 * @since 1.0.0
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    private final String headerName;
    private final String mdcKey;
    private final boolean generateIfAbsent;

    /**
     * Constructs the filter from the resolved correlation settings.
     *
     * @param correlation the correlation configuration; must not be {@code null}
     */
    public CorrelationIdFilter(ObservabilityProperties.Correlation correlation) {
        this.headerName = correlation.getHeaderName();
        this.mdcKey = correlation.getMdcKey();
        this.generateIfAbsent = correlation.isGenerateIfAbsent();
        CorrelationContext.setMdcKey(this.mdcKey);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the correlation ID, binds it to the MDC and response, delegates down the chain,
     * and clears the MDC entry afterwards.
     *
     * @param request     the current request; must not be {@code null}
     * @param response     the current response; must not be {@code null}
     * @param filterChain the remaining filter chain; must not be {@code null}
     * @throws ServletException if the downstream chain raises a servlet error
     * @throws IOException      if the downstream chain raises an I/O error
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(headerName);
        if (!StringUtils.hasText(correlationId) && generateIfAbsent) {
            correlationId = UUID.randomUUID().toString();
        }

        boolean bound = StringUtils.hasText(correlationId);
        if (bound) {
            MDC.put(mdcKey, correlationId);
            response.setHeader(headerName, correlationId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (bound) {
                MDC.remove(mdcKey);
            }
        }
    }
}
