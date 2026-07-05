/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import io.github.saumilp.starters.observability.config.ObservabilityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test exercising {@link CorrelationIdFilter} inside the Spring MVC dispatch pipeline
 * via {@link MockMvc}. Requires no external infrastructure.
 */
class CorrelationIdFilterMvcIntegrationTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new PingController())
        .addFilters(new CorrelationIdFilter(new ObservabilityProperties.Correlation()))
        .build();

    @Test
    void should_echoProvidedCorrelationId_onResponse() throws Exception {
        mockMvc.perform(get("/ping").header("X-Correlation-Id", "req-77"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Correlation-Id", "req-77"));
    }

    @Test
    void should_generateCorrelationId_when_absent() throws Exception {
        mockMvc.perform(get("/ping"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @RestController
    static class PingController {
        @GetMapping("/ping")
        String ping() {
            return "pong";
        }
    }
}
