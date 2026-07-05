/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.model;

/**
 * The delivery channel a {@link NotificationMessage} targets.
 *
 * <p>Provider starters (Twilio, Resend, OneSignal, Novu, ...) each declare which channels they
 * support via {@link io.github.saumilp.starters.notifications.spi.NotificationSender#supports}.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public enum Channel {

    /** Transactional or marketing email. */
    EMAIL,

    /** Short Message Service (text) delivery. */
    SMS,

    /** Mobile / web push notification. */
    PUSH,

    /** In-application message (inbox / bell feed). */
    IN_APP,

    /** WhatsApp Business message. */
    WHATSAPP
}
