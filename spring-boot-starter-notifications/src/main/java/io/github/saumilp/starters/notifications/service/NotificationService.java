/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.service;

import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;
import io.github.saumilp.starters.notifications.spi.NotificationSender;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * High-level façade applications inject to send notifications.
 *
 * <p>Delegates to the auto-configured {@link NotificationSender} (normally a
 * {@link io.github.saumilp.starters.notifications.sender.CompositeNotificationSender}) and offers
 * both synchronous {@link #send} and asynchronous {@link #sendAsync} dispatch. Async dispatch runs
 * on the supplied {@link Executor}.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class NotificationService {

    private final NotificationSender sender;
    private final Executor executor;

    /**
     * Creates a notification service.
     *
     * @param sender   the underlying sender; must not be {@code null}
     * @param executor the executor used for {@link #sendAsync}; must not be {@code null}
     */
    public NotificationService(NotificationSender sender, Executor executor) {
        this.sender = sender;
        this.executor = executor;
    }

    /**
     * Sends the message synchronously on the calling thread.
     *
     * @param message the message to send; must not be {@code null}
     * @return the delivery outcome; never {@code null}
     */
    public NotificationResult send(NotificationMessage message) {
        return sender.send(message);
    }

    /**
     * Sends the message asynchronously on the configured executor.
     *
     * @param message the message to send; must not be {@code null}
     * @return a future completing with the delivery outcome; never {@code null}
     */
    public CompletableFuture<NotificationResult> sendAsync(NotificationMessage message) {
        return CompletableFuture.supplyAsync(() -> sender.send(message), executor);
    }
}
