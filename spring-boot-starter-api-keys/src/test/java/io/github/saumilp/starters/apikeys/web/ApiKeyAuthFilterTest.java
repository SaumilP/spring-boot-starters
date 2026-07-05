/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.apikeys.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.apikeys.hash.ApiKeyHasher;
import io.github.saumilp.starters.apikeys.model.ApiKey;
import io.github.saumilp.starters.apikeys.model.IssuedApiKey;
import io.github.saumilp.starters.apikeys.service.ApiKeyService;
import io.github.saumilp.starters.apikeys.store.InMemoryApiKeyStore;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiKeyAuthFilterTest {

    private ApiKeyService service;
    private ApiKeyAuthFilter filter;
    private String validKey;

    @BeforeEach
    void setUp() {
        service = new ApiKeyService(new InMemoryApiKeyStore(), new ApiKeyHasher("SHA-256"), 32, "sk");
        filter = new ApiKeyAuthFilter(service, "X-Api-Key", List.of("/internal/**"));
        IssuedApiKey issued = service.issue("tester", Set.of());
        validKey = issued.plaintext();
    }

    @Test
    void should_passThrough_when_pathNotProtected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/public/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void should_reject_when_protectedAndNoKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void should_allowAndExposePrincipal_when_protectedAndValidKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/data");
        request.addHeader("X-Api-Key", validKey);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        ApiKey principal = (ApiKey) request.getAttribute(ApiKeyAuthFilter.PRINCIPAL_ATTRIBUTE);
        assertThat(principal).isNotNull();
        assertThat(principal.principal()).isEqualTo("tester");
    }
}
