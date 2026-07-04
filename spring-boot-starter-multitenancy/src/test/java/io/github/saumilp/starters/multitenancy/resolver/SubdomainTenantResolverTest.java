/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.resolver;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SubdomainTenantResolver}.
 */
class SubdomainTenantResolverTest {

    private final SubdomainTenantResolver resolver = new SubdomainTenantResolver();

    @Test
    void should_returnSubdomain_when_threePartHostPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("acme.example.com");
        assertThat(resolver.resolveTenant(request)).isEqualTo("acme");
    }

    @Test
    void should_returnNull_when_bareHostname() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("example.com");
        assertThat(resolver.resolveTenant(request)).isNull();
    }

    @Test
    void should_returnNull_when_localhost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        assertThat(resolver.resolveTenant(request)).isNull();
    }
}
