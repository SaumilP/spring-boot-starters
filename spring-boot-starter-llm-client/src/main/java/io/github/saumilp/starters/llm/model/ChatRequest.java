/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a chat completion request in the OpenAI Chat Completions API format.
 *
 * <p>Use {@link #of(String, List)} for quick construction with sensible defaults
 * ({@code temperature=0.7}, {@code maxTokens=1024}).
 *
 * @param model       the model identifier (e.g., {@code "gpt-4o-mini"}); must not be blank
 * @param messages    the ordered list of conversation messages; must not be empty
 * @param temperature the sampling temperature in the range {@code [0, 2]}; defaults to {@code 0.7}
 * @param maxTokens   the maximum number of tokens to generate; defaults to {@code 1024}
 *
 * @since 1.0.0
 */
public record ChatRequest(
    @JsonProperty("model")       String          model,
    @JsonProperty("messages")    List<ChatMessage> messages,
    @JsonProperty("temperature") double          temperature,
    @JsonProperty("max_tokens")  int             maxTokens
) {

    /**
     * Compact canonical constructor — validates required fields.
     *
     * @throws IllegalArgumentException if {@code model} is blank or {@code messages} is empty
     */
    public ChatRequest {
        if (model == null || model.isBlank())
            throw new IllegalArgumentException("model must not be blank");
        if (messages == null || messages.isEmpty())
            throw new IllegalArgumentException("messages must not be empty");
        messages = List.copyOf(messages);
    }

    /**
     * Creates a {@link ChatRequest} with the given model and messages using default
     * sampling parameters ({@code temperature=0.7}, {@code maxTokens=1024}).
     *
     * @param model    the model identifier; must not be blank
     * @param messages the conversation messages; must not be empty
     * @return a new {@link ChatRequest}; never {@code null}
     */
    public static ChatRequest of(String model, List<ChatMessage> messages) {
        return new ChatRequest(model, messages, 0.7, 1024);
    }
}
