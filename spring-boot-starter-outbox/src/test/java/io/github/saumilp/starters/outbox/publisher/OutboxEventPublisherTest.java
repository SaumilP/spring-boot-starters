/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.publisher;

import io.github.saumilp.starters.outbox.model.OutboxEvent;
import io.github.saumilp.starters.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OutboxEventPublisher}.
 */
@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxEventPublisher(repository);
        when(repository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void should_persistEventWithCorrectFields_when_publishCalled() {
        publisher.publish("Order", "order-42", "ORDER_CREATED", "{\"id\":42}");

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getAggregateType()).isEqualTo("Order");
        assertThat(saved.getAggregateId()).isEqualTo("order-42");
        assertThat(saved.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getPayload()).isEqualTo("{\"id\":42}");
    }

    @Test
    void should_returnSavedEvent_when_publishSucceeds() {
        OutboxEvent result = publisher.publish("User", "user-1", "USER_CREATED", "{}");
        assertThat(result).isNotNull();
        assertThat(result.getAggregateType()).isEqualTo("User");
    }

    @Test
    void should_delegateToRepository_when_publishCalled() {
        publisher.publish("Product", "prod-99", "PRODUCT_UPDATED", "{\"price\":9.99}");
        verify(repository).save(any(OutboxEvent.class));
    }
}
