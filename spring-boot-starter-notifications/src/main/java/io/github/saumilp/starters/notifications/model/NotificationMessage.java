/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.model;

import java.util.Map;

/**
 * An immutable, provider-agnostic notification request.
 *
 * <p>A message names a {@code recipient} (an email address, phone number in E.164 form, device
 * token, or subscriber ID depending on the {@link Channel}), the target {@code channel}, an
 * optional {@code subject} and {@code body}, an optional {@code templateRef} (when the provider
 * renders from a named template instead of an inline body), and free-form {@code metadata} passed
 * through to the provider.
 *
 * @param recipient   the channel-specific address of the recipient; must not be {@code null} or blank
 * @param channel     the delivery channel; must not be {@code null}
 * @param subject     the message subject (used by {@link Channel#EMAIL}); may be {@code null}
 * @param body        the inline message body; may be {@code null} when {@code templateRef} is set
 * @param templateRef a provider template identifier to render instead of {@code body}; may be {@code null}
 * @param metadata    provider-specific extra attributes; never {@code null} (empty when unset)
 * @author SaumilP
 * @since 1.0.0
 */
public record NotificationMessage(
        String recipient,
        Channel channel,
        String subject,
        String body,
        String templateRef,
        Map<String, Object> metadata) {

    /**
     * Canonical constructor that validates required fields and defends {@code metadata}.
     *
     * @param recipient   the channel-specific address of the recipient; must not be {@code null} or blank
     * @param channel     the delivery channel; must not be {@code null}
     * @param subject     the message subject; may be {@code null}
     * @param body        the inline message body; may be {@code null}
     * @param templateRef a provider template identifier; may be {@code null}
     * @param metadata    provider-specific extra attributes; {@code null} is normalised to an empty map
     */
    public NotificationMessage {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be null or blank");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * Creates a new builder.
     *
     * @return a fresh {@link Builder}; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link NotificationMessage}.
     *
     * @author SaumilP
     * @since 1.0.0
     */
    public static final class Builder {

        private String recipient;
        private Channel channel;
        private String subject;
        private String body;
        private String templateRef;
        private Map<String, Object> metadata = Map.of();

        private Builder() {
        }

        /**
         * Sets the recipient address.
         *
         * @param recipient the channel-specific recipient address
         * @return this builder; never {@code null}
         */
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * Sets the delivery channel.
         *
         * @param channel the target channel
         * @return this builder; never {@code null}
         */
        public Builder channel(Channel channel) {
            this.channel = channel;
            return this;
        }

        /**
         * Sets the subject.
         *
         * @param subject the message subject
         * @return this builder; never {@code null}
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets the inline body.
         *
         * @param body the message body
         * @return this builder; never {@code null}
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Sets the provider template reference.
         *
         * @param templateRef the template identifier
         * @return this builder; never {@code null}
         */
        public Builder templateRef(String templateRef) {
            this.templateRef = templateRef;
            return this;
        }

        /**
         * Sets the provider metadata map.
         *
         * @param metadata the metadata map; {@code null} is treated as empty
         * @return this builder; never {@code null}
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata == null ? Map.of() : metadata;
            return this;
        }

        /**
         * Builds the immutable message.
         *
         * @return a new {@link NotificationMessage}; never {@code null}
         */
        public NotificationMessage build() {
            return new NotificationMessage(recipient, channel, subject, body, templateRef, metadata);
        }
    }
}
