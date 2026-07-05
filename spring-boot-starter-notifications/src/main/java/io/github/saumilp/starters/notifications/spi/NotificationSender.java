/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.spi;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;

/**
 * Strategy interface implemented by each notification transport.
 *
 * <p>This is the core SPI of the notifications starter. Provider starters (Twilio, Resend,
 * OneSignal, Novu, ...) register a {@code NotificationSender} bean declaring the channels they
 * support. The auto-configured
 * {@link io.github.saumilp.starters.notifications.sender.CompositeNotificationSender} collects
 * every sender bean and routes each message to the first one that supports its channel.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public interface NotificationSender {

    /**
     * Reports whether this sender can deliver messages on the given channel.
     *
     * @param channel the target channel; never {@code null}
     * @return {@code true} if this sender handles {@code channel}
     */
    boolean supports(Channel channel);

    /**
     * Delivers the message.
     *
     * <p>Implementations must not throw for expected delivery failures — return a
     * {@link NotificationResult#failure} instead so a composite can continue routing.
     *
     * @param message the message to deliver; never {@code null}
     * @return the delivery outcome; never {@code null}
     */
    NotificationResult send(NotificationMessage message);

    /**
     * A short, stable name for this sender used in logs and metrics.
     *
     * @return the sender name; defaults to the simple class name
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
