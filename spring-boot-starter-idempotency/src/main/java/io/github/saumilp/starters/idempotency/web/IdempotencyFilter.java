/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.web;

import io.github.saumilp.starters.idempotency.config.IdempotencyProperties;
import io.github.saumilp.starters.idempotency.model.CachedResponse;
import io.github.saumilp.starters.idempotency.store.IdempotencyStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servlet filter that enforces HTTP idempotency using the {@code Idempotency-Key} request header.
 *
 * <h2>Behaviour</h2>
 * <ol>
 *   <li>If the request does not contain an {@code Idempotency-Key} header, it passes through
 *       unmodified — idempotency is opt-in per request.</li>
 *   <li>If a cached response exists for the key, it is replayed immediately with an
 *       {@code X-Idempotency-Replayed: true} header added.</li>
 *   <li>If no cached response exists:
 *     <ol type="a">
 *       <li>The filter attempts to acquire the per-key lock via
 *           {@link IdempotencyStore#tryLock}.</li>
 *       <li>If the lock is already held (concurrent duplicate request), HTTP
 *           {@code 409 Conflict} is returned immediately.</li>
 *       <li>Otherwise the request proceeds through the filter chain, the response is
 *           captured, stored in the idempotency store, and the lock is released.</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p>Only the HTTP methods listed in
 * {@link IdempotencyProperties#getApplicableMethods()} are subject to idempotency
 * enforcement (defaults to {@code POST} and {@code PATCH}).
 *
 * @since 1.0.0
 */
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String HEADER_REPLAYED = "X-Idempotency-Replayed";

    private final IdempotencyStore store;
    private final IdempotencyProperties props;

    /**
     * Constructs the filter with the given store and configuration.
     *
     * @param store the backing idempotency store; must not be {@code null}
     * @param props the starter configuration properties; must not be {@code null}
     */
    public IdempotencyFilter(IdempotencyStore store, IdempotencyProperties props) {
        this.store = store;
        this.props = props;
    }

    /**
     * Intercepts each request once and applies idempotency logic as described in the class
     * documentation.
     *
     * @param request     the current HTTP request; never {@code null}
     * @param response    the current HTTP response; never {@code null}
     * @param filterChain the remaining filter chain; never {@code null}
     * @throws ServletException if the filter chain raises a servlet error
     * @throws IOException      if an I/O error occurs reading or writing the response
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String idempotencyKey = request.getHeader(props.getHeaderName());

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || !isApplicableMethod(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Serve from cache if available
        Optional<CachedResponse> cached = store.get(idempotencyKey);
        if (cached.isPresent()) {
            log.debug("Replaying cached response for Idempotency-Key '{}'", idempotencyKey);
            replayResponse(cached.get(), response);
            return;
        }

        // Try to acquire the in-progress lock
        Duration lockTimeout = Duration.ofSeconds(props.getLockTimeoutSeconds());
        if (!store.tryLock(idempotencyKey, lockTimeout)) {
            log.warn("Concurrent request detected for Idempotency-Key '{}'; returning 409", idempotencyKey);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"status\":409,\"error\":\"Conflict\"," +
                "\"message\":\"A request with this Idempotency-Key is already in progress.\"}");
            return;
        }

        // Execute, capture, and cache
        IdempotencyResponseWrapper wrapper = new IdempotencyResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
            wrapper.flushBuffer();

            CachedResponse toCache = new CachedResponse(
                wrapper.getStatus(),
                captureHeaders(wrapper),
                wrapper.getCapturedBody(),
                Instant.now(),
                idempotencyKey
            );
            store.put(idempotencyKey, toCache, Duration.ofSeconds(props.getTtlSeconds()));
            log.debug("Cached response for Idempotency-Key '{}', status={}",
                idempotencyKey, wrapper.getStatus());
        } finally {
            store.unlock(idempotencyKey);
        }
    }

    private boolean isApplicableMethod(String method) {
        return props.getApplicableMethods().stream()
            .anyMatch(m -> m.equalsIgnoreCase(method));
    }

    private void replayResponse(CachedResponse cached, HttpServletResponse response) throws IOException {
        response.setStatus(cached.statusCode());
        cached.headers().forEach(response::setHeader);
        response.setHeader(HEADER_REPLAYED, "true");
        response.getWriter().write(cached.body());
        response.flushBuffer();
    }

    private Map<String, String> captureHeaders(HttpServletResponse response) {
        return response.getHeaderNames().stream()
            .collect(Collectors.toMap(name -> name, response::getHeader, (a, b) -> a));
    }
}
