/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.config;

import io.github.saumilp.starters.notifications.model.Channel;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the notifications starter, bound from
 * {@code spring.notifications.*}.
 *
 * <pre>{@code
 * spring:
 *   notifications:
 *     enabled: true
 *     default-channel: EMAIL
 *     logging-sender:
 *       enabled: true
 * }</pre>
 *
 * @author SaumilP
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.notifications")
public class NotificationsProperties {

    /** Whether the notifications wiring is enabled. */
    private boolean enabled = true;

    /** The channel assumed when a caller does not specify one. */
    private Channel defaultChannel = Channel.EMAIL;

    /** Settings for the built-in logging fallback sender. */
    private final LoggingSender loggingSender = new LoggingSender();

    /** Creates an instance with default values. */
    public NotificationsProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable all notification wiring
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the default channel} */
    public Channel getDefaultChannel() {
        return defaultChannel;
    }

    /**
     * Sets the default channel.
     *
     * @param defaultChannel the channel to assume when unspecified
     */
    public void setDefaultChannel(Channel defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    /** {@return the logging-sender settings} */
    public LoggingSender getLoggingSender() {
        return loggingSender;
    }

    /**
     * Settings for the built-in logging fallback sender.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static class LoggingSender {

        /** Whether the logging fallback sender is registered. */
        private boolean enabled = true;

        /** Creates an instance with default values. */
        public LoggingSender() {
        }

        /** {@return whether the logging fallback sender is enabled} */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether the logging fallback sender is registered.
         *
         * @param enabled {@code false} to omit the logging sender
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
