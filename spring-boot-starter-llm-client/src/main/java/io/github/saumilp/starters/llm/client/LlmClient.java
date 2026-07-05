/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.client;

import io.github.saumilp.starters.llm.exception.LlmClientException;
import io.github.saumilp.starters.llm.model.ChatMessage;
import io.github.saumilp.starters.llm.model.ChatRequest;
import io.github.saumilp.starters.llm.model.ChatResponse;

import java.util.List;

/**
 * Abstraction for an OpenAI-compatible large language model client.
 *
 * <p>The primary method is {@link #chat(ChatRequest)}, which sends a structured request
 * to the configured endpoint and returns the full response. The convenience method
 * {@link #ask(String)} wraps a single user message and returns only the text content,
 * covering the most common use case in a single call.
 *
 * <p>The default implementation is {@link RestClientLlmClient}, registered automatically
 * by the starter. Applications can replace it by declaring their own {@code LlmClient} bean.
 *
 * @since 1.0.0
 */
public interface LlmClient {

    /**
     * Sends a chat completion request to the LLM endpoint and returns the full response.
     *
     * <p>The implementation may retry the request up to a configured number of times on
     * transient failures before throwing {@link LlmClientException}.
     *
     * @param request the chat request containing the model, messages, and parameters;
     *                must not be {@code null}
     * @return the LLM completion response; never {@code null}
     * @throws LlmClientException if the request fails after all retry attempts
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Sends a single user message using the configured default model and returns the
     * generated text content.
     *
     * <p>This is a convenience wrapper for the common case of a single-turn, single-message
     * interaction. It is equivalent to:
     * <pre>{@code
     * chat(ChatRequest.of(defaultModel, List.of(ChatMessage.user(userMessage)))).firstContent()
     * }</pre>
     *
     * @param userMessage the user message content; must not be {@code null} or blank
     * @return the generated response text; never {@code null}, may be empty if the model
     *         returned no choices
     * @throws LlmClientException if the request fails
     */
    default String ask(String userMessage) {
        return chat(ChatRequest.of("default", List.of(ChatMessage.user(userMessage)))).firstContent();
    }
}
