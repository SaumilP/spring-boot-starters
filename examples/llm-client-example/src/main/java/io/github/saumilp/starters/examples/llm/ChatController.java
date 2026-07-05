/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.llm;

import io.github.saumilp.starters.llm.client.LlmClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple chat endpoint demonstrating {@link LlmClient#ask(String)}.
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final LlmClient llmClient;

    public ChatController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String message  = body.getOrDefault("message", "Hello!");
        String response = llmClient.ask(message);
        return Map.of("message", message, "response", response);
    }
}
