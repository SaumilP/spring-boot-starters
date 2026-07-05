/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single message in a chat conversation, following the OpenAI Chat Completions
 * message format.
 *
 * <p>Each message has a {@code role} (e.g., {@code "system"}, {@code "user"},
 * {@code "assistant"}) and a {@code content} string. Use the factory methods
 * {@link #user(String)}, {@link #system(String)}, and {@link #assistant(String)} for
 * convenient construction.
 *
 * @param role    the message author role; must not be {@code null} or blank
 * @param content the message text content; must not be {@code null}
 *
 * @since 1.0.0
 */
public record ChatMessage(
    @JsonProperty("role")    String role,
    @JsonProperty("content") String content
) {

    /**
     * Compact canonical constructor — validates required fields.
     *
     * @throws IllegalArgumentException if {@code role} is blank or {@code content} is {@code null}
     */
    public ChatMessage {
        if (role == null || role.isBlank()) throw new IllegalArgumentException("role must not be blank");
        if (content == null) throw new IllegalArgumentException("content must not be null");
    }

    /**
     * Creates a user-role message with the given content.
     *
     * @param content the message text; must not be {@code null}
     * @return a new {@link ChatMessage} with role {@code "user"}
     */
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    /**
     * Creates a system-role message with the given content.
     *
     * @param content the system prompt text; must not be {@code null}
     * @return a new {@link ChatMessage} with role {@code "system"}
     */
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    /**
     * Creates an assistant-role message with the given content.
     *
     * @param content the assistant response text; must not be {@code null}
     * @return a new {@link ChatMessage} with role {@code "assistant"}
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}
