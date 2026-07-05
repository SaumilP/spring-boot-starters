/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.relay;

import io.github.saumilp.starters.outbox.broker.MessageBrokerAdapter;
import io.github.saumilp.starters.outbox.config.OutboxProperties;
import io.github.saumilp.starters.outbox.model.OutboxEvent;
import io.github.saumilp.starters.outbox.model.OutboxEventStatus;
import io.github.saumilp.starters.outbox.repository.OutboxEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OutboxEventRelay} relay behaviour.
 */
@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private MessageBrokerAdapter broker;

    private OutboxProperties props;
    private OutboxEventRelay relay;

    @BeforeEach
    void setUp() {
        props = new OutboxProperties();
        props.setMaxRetries(3);
        relay = new OutboxEventRelay(repository, broker, props);
        when(repository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private OutboxEvent pendingEvent() {
        OutboxEvent event = new OutboxEvent();
        event.setCreatedAt(Instant.now());
        event.setStatus(OutboxEventStatus.PENDING);
        event.setAggregateType("Order");
        event.setAggregateId("1");
        event.setEventType("ORDER_CREATED");
        event.setPayload("{}");
        return event;
    }

    @Test
    void should_markProcessed_when_brokerSendSucceeds() throws Exception {
        OutboxEvent event = pendingEvent();
        when(repository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            eq(OutboxEventStatus.PENDING), eq(3))).thenReturn(List.of(event));

        relay.relay();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PROCESSED);
        assertThat(event.getProcessedAt()).isNotNull();
        verify(broker).send(event);
    }

    @Test
    void should_incrementRetryCount_when_brokerSendFails_and_retriesRemaining() throws Exception {
        OutboxEvent event = pendingEvent();
        when(repository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            eq(OutboxEventStatus.PENDING), eq(3))).thenReturn(List.of(event));
        doThrow(new RuntimeException("broker down")).when(broker).send(event);

        relay.relay();

        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getErrorMessage()).isEqualTo("broker down");
    }

    @Test
    void should_markFailed_when_retryCountReachesMax() throws Exception {
        OutboxEvent event = pendingEvent();
        event.setRetryCount(2); // one more attempt will hit maxRetries=3
        when(repository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            eq(OutboxEventStatus.PENDING), eq(3))).thenReturn(List.of(event));
        doThrow(new RuntimeException("permanent failure")).when(broker).send(event);

        relay.relay();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(3);
    }

    @Test
    void should_saveUpdatedEvent_regardless_of_outcome() throws Exception {
        OutboxEvent event = pendingEvent();
        when(repository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            eq(OutboxEventStatus.PENDING), eq(3))).thenReturn(List.of(event));

        relay.relay();

        verify(repository).save(event);
    }
}
