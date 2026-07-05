/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ChatMessage} construction, validation, and factory methods.
 */
class ChatMessageTest {

    @Test
    void should_setUserRole_when_usingUserFactory() {
        ChatMessage msg = ChatMessage.user("Hello");
        assertThat(msg.role()).isEqualTo("user");
        assertThat(msg.content()).isEqualTo("Hello");
    }

    @Test
    void should_setSystemRole_when_usingSystemFactory() {
        ChatMessage msg = ChatMessage.system("You are a helpful assistant.");
        assertThat(msg.role()).isEqualTo("system");
        assertThat(msg.content()).isEqualTo("You are a helpful assistant.");
    }

    @Test
    void should_setAssistantRole_when_usingAssistantFactory() {
        ChatMessage msg = ChatMessage.assistant("I can help with that.");
        assertThat(msg.role()).isEqualTo("assistant");
        assertThat(msg.content()).isEqualTo("I can help with that.");
    }

    @Test
    void should_throwException_when_contentNull() {
        assertThatThrownBy(() -> ChatMessage.user(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("content");
    }

    @Test
    void should_throwException_when_roleBlank() {
        assertThatThrownBy(() -> new ChatMessage("  ", "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("role");
    }

    @Test
    void should_throwException_when_roleNull() {
        assertThatThrownBy(() -> new ChatMessage(null, "content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("role");
    }
}
