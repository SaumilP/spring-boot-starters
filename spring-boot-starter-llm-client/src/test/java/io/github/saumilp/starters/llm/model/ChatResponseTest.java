/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ChatResponse} convenience methods.
 */
class ChatResponseTest {

    @Test
    void should_returnFirstChoiceContent_when_choicesPresent() {
        ChatMessage msg = ChatMessage.assistant("Paris");
        ChatResponse.Choice choice = new ChatResponse.Choice(0, msg, "stop");
        ChatResponse response = new ChatResponse("id-1", "gpt-4o-mini", List.of(choice), null);
        assertThat(response.firstContent()).isEqualTo("Paris");
    }

    @Test
    void should_returnEmptyString_when_choicesEmpty() {
        ChatResponse response = new ChatResponse("id-2", "gpt-4o-mini", List.of(), null);
        assertThat(response.firstContent()).isEmpty();
    }

    @Test
    void should_returnEmptyString_when_choicesNull() {
        ChatResponse response = new ChatResponse("id-3", "gpt-4o-mini", null, null);
        assertThat(response.firstContent()).isEmpty();
    }

    @Test
    void should_returnEmptyString_when_messageContentNull() {
        ChatResponse.Choice choice = new ChatResponse.Choice(0, new ChatMessage("assistant", ""), "stop");
        ChatResponse response = new ChatResponse("id-4", "gpt-4o-mini", List.of(choice), null);
        assertThat(response.firstContent()).isEmpty();
    }
}
