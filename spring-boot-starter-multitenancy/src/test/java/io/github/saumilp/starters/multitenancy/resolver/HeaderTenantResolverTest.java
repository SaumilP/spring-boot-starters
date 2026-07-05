/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.resolver;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HeaderTenantResolver}.
 */
class HeaderTenantResolverTest {

    @Test
    void should_returnTenantId_when_headerPresent() {
        HeaderTenantResolver resolver = new HeaderTenantResolver("X-Tenant-Id");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant-Id", "acme");
        assertThat(resolver.resolveTenant(request)).isEqualTo("acme");
    }

    @Test
    void should_returnNull_when_headerAbsent() {
        HeaderTenantResolver resolver = new HeaderTenantResolver("X-Tenant-Id");
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(resolver.resolveTenant(request)).isNull();
    }

    @Test
    void should_respectCustomHeaderName() {
        HeaderTenantResolver resolver = new HeaderTenantResolver("X-Org-Id");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Org-Id", "beta-corp");
        assertThat(resolver.resolveTenant(request)).isEqualTo("beta-corp");
        assertThat(resolver.getHeaderName()).isEqualTo("X-Org-Id");
    }
}
