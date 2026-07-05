/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.sender;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;
import io.github.saumilp.starters.notifications.spi.NotificationSender;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link NotificationSender} that routes each message to the first registered delegate that
 * supports the message's {@link Channel}.
 *
 * <p>This is the primary sender registered by the auto-configuration. All {@code NotificationSender}
 * beans discovered in the application context (excluding this composite) become delegates. If no
 * delegate supports the requested channel, a {@link NotificationResult#failure} is returned rather
 * than throwing.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class CompositeNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(CompositeNotificationSender.class);

    private final List<NotificationSender> delegates;

    /**
     * Constructs a composite over the given delegates.
     *
     * @param delegates the ordered delegate senders; must not be {@code null}
     */
    public CompositeNotificationSender(List<NotificationSender> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public boolean supports(Channel channel) {
        return delegates.stream().anyMatch(d -> d.supports(channel));
    }

    @Override
    public NotificationResult send(NotificationMessage message) {
        for (NotificationSender delegate : delegates) {
            if (delegate.supports(message.channel())) {
                log.debug("Routing {} message to sender '{}'", message.channel(), delegate.name());
                return delegate.send(message);
            }
        }
        String detail = "No NotificationSender supports channel " + message.channel();
        log.warn(detail);
        return NotificationResult.failure(message.channel(), detail);
    }

    /**
     * Returns the number of delegate senders in this composite.
     *
     * @return the delegate count; always non-negative
     */
    public int delegateCount() {
        return delegates.size();
    }
}
