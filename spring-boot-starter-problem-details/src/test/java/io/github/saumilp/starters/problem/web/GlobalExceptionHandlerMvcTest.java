/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.problem.web;

import io.github.saumilp.starters.problem.config.ProblemDetailsProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests {@link GlobalExceptionHandler} through the Spring MVC pipeline via {@link MockMvc}.
 */
class GlobalExceptionHandlerMvcTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new TestController())
        .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailsProperties()))
        .build();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_return400ProblemWithFieldErrors_when_validationFails() throws Exception {
        mockMvc.perform(post("/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("validation-error"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void should_preserveStatus_when_responseStatusExceptionThrown() throws Exception {
        mockMvc.perform(get("/missing"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("request-error"))
            .andExpect(jsonPath("$.detail").value("no such thing"));
    }

    @Test
    void should_return500Problem_when_unexpectedException() throws Exception {
        mockMvc.perform(get("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("internal-error"));
    }

    @Test
    void should_includeCorrelationId_when_boundToMdc() throws Exception {
        MDC.put("correlationId", "cid-123");
        mockMvc.perform(get("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.correlationId").value("cid-123"));
    }

    @RestController
    static class TestController {

        @PostMapping("/create")
        String create(@Valid @RequestBody CreateRequest request) {
            return "ok";
        }

        @GetMapping("/missing")
        String missing() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no such thing");
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("kaboom");
        }
    }

    record CreateRequest(@NotBlank String name) {
    }
}
