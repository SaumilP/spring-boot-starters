/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.web;

import io.github.saumilp.starters.apikeys.model.ApiKey;
import io.github.saumilp.starters.apikeys.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that enforces a valid API key on the configured protected paths.
 *
 * <p>Reads the key from the configured header (default {@code X-Api-Key}), validates it via
 * {@link ApiKeyService}, and on success exposes the resolved {@link ApiKey} as the request attribute
 * {@link #PRINCIPAL_ATTRIBUTE}. Requests to non-protected paths pass through untouched; protected
 * requests with a missing or invalid key receive {@code 401} with a small JSON body.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    /** Request attribute holding the authenticated {@link ApiKey} after a successful match. */
    public static final String PRINCIPAL_ATTRIBUTE = "io.github.saumilp.apikeys.principal";

    private final ApiKeyService service;
    private final String headerName;
    private final List<String> protectedPaths;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Creates the filter.
     *
     * @param service        the validation service; must not be {@code null}
     * @param headerName     the header carrying the API key; must not be {@code null}
     * @param protectedPaths Ant-style paths that require a key; must not be {@code null}
     */
    public ApiKeyAuthFilter(ApiKeyService service, String headerName, List<String> protectedPaths) {
        this.service = service;
        this.headerName = headerName;
        this.protectedPaths = List.copyOf(protectedPaths);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return protectedPaths.stream().noneMatch(p -> matcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(headerName);
        Optional<ApiKey> key = service.validate(provided);
        if (key.isEmpty()) {
            reject(response);
            return;
        }
        request.setAttribute(PRINCIPAL_ATTRIBUTE, key.get());
        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing or invalid API key\"}");
    }
}
