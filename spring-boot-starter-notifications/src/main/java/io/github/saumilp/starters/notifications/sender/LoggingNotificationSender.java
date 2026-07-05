/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.sender;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;
import io.github.saumilp.starters.notifications.spi.NotificationSender;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default {@link NotificationSender} that logs the message instead of delivering it.
 *
 * <p>It supports every {@link Channel}, so the core starter is usable end-to-end with no provider
 * on the classpath — useful for local development, tests, and as a safe fallback. It is registered
 * only when no provider sender is present and
 * {@code spring.notifications.logging-sender.enabled} is {@code true} (the default).
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    /** Creates a logging sender. */
    public LoggingNotificationSender() {
    }

    @Override
    public boolean supports(Channel channel) {
        return channel != null;
    }

    @Override
    public NotificationResult send(NotificationMessage message) {
        String id = UUID.randomUUID().toString();
        log.info("[notifications] channel={} recipient={} subject={} templateRef={} id={}",
            message.channel(), message.recipient(), message.subject(), message.templateRef(), id);
        return NotificationResult.success(message.channel(), id);
    }

    @Override
    public String name() {
        return "logging";
    }
}
