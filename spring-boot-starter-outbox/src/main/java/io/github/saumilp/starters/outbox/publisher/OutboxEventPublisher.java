/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.outbox.publisher;

import io.github.saumilp.starters.outbox.model.OutboxEvent;
import io.github.saumilp.starters.outbox.repository.OutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publishes domain events to the outbox table within the caller's current transaction.
 *
 * <p>The caller invokes {@link #publish} inside a {@code @Transactional} service method.
 * Because the outbox row is written within the same transaction as the business data, the
 * two changes are committed atomically — either both persist or neither does. This eliminates
 * the dual-write problem that arises when publishing directly to a message broker inside a
 * database transaction.
 *
 * <p>The {@link io.github.saumilp.starters.outbox.relay.OutboxEventRelay} later polls the
 * {@code outbox_events} table and forwards committed rows to the configured broker.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Transactional
 * public Order createOrder(CreateOrderCommand cmd) {
 *     Order order = orderRepository.save(new Order(cmd));
 *     publisher.publish("Order", order.getId().toString(),
 *                       "ORDER_CREATED", toJson(order));
 *     return order;
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Transactional
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;

    /**
     * Constructs the publisher with the given repository.
     *
     * @param repository the JPA repository for persisting outbox events; must not be {@code null}
     */
    public OutboxEventPublisher(OutboxEventRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a new outbox event with {@link io.github.saumilp.starters.outbox.model.OutboxEventStatus#PENDING}
     * status within the current transaction.
     *
     * <p>This method must be called inside an active transaction (i.e., from a
     * {@code @Transactional} method or within a programmatically managed transaction
     * boundary). The event will only become visible to the relay after the surrounding
     * transaction commits.
     *
     * @param aggregateType the domain aggregate type (e.g., {@code "Order"});
     *                      must not be {@code null} or blank
     * @param aggregateId   the identifier of the aggregate instance (e.g., the order ID);
     *                      must not be {@code null} or blank
     * @param eventType     the event type in upper-snake-case (e.g., {@code "ORDER_CREATED"});
     *                      must not be {@code null} or blank
     * @param payload       the JSON-serialised event payload; must not be {@code null}
     * @return the persisted {@link OutboxEvent} with its generated ID and timestamps set
     */
    public OutboxEvent publish(String aggregateType, String aggregateId,
                               String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        return repository.save(event);
    }
}
