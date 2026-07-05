/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a chat completion response in the OpenAI Chat Completions API format.
 *
 * <p>Use {@link #firstContent()} as a convenience method to extract the text of the
 * first generated choice without navigating the nested structure.
 *
 * @param id      the unique completion ID assigned by the LLM provider
 * @param model   the model that generated the response
 * @param choices the list of generated completions; typically contains one entry
 * @param usage   token usage statistics for the request; may be {@code null} if the
 *                provider does not return usage data
 *
 * @since 1.0.0
 */
public record ChatResponse(
    @JsonProperty("id")      String       id,
    @JsonProperty("model")   String       model,
    @JsonProperty("choices") List<Choice> choices,
    @JsonProperty("usage")   Usage        usage
) {

    /**
     * A single generated completion choice.
     *
     * @param index        the zero-based index of this choice in the response
     * @param message      the generated message
     * @param finishReason the reason the model stopped generating (e.g., {@code "stop"},
     *                     {@code "length"}); may be {@code null}
     */
    public record Choice(
        @JsonProperty("index")         int         index,
        @JsonProperty("message")       ChatMessage message,
        @JsonProperty("finish_reason") String      finishReason
    ) {}

    /**
     * Token consumption statistics for the completed request.
     *
     * @param promptTokens     the number of tokens in the input prompt
     * @param completionTokens the number of tokens in the generated output
     * @param totalTokens      the sum of prompt and completion tokens
     */
    public record Usage(
        @JsonProperty("prompt_tokens")     int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens,
        @JsonProperty("total_tokens")      int totalTokens
    ) {}

    /**
     * Returns the text content of the first generated choice.
     *
     * <p>This convenience method avoids null-checking the {@link #choices()} list and
     * the nested {@link ChatMessage#content()} in common single-completion use cases.
     *
     * @return the content string of the first choice; empty string if there are no choices
     *         or the message content is {@code null}
     */
    public String firstContent() {
        if (choices == null || choices.isEmpty()) return "";
        ChatMessage msg = choices.get(0).message();
        return msg != null && msg.content() != null ? msg.content() : "";
    }
}
