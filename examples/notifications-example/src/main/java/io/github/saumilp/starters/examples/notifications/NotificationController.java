/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.notifications;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;
import io.github.saumilp.starters.notifications.service.NotificationService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sends a notification through the {@link NotificationService} façade.
 *
 * @author SaumilP
 */
@RestController
public class NotificationController {

    private final NotificationService notifications;

    /**
     * Creates the controller.
     *
     * @param notifications the notification service; must not be {@code null}
     */
    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    /**
     * Sends a message on the requested channel.
     *
     * @param request a JSON body with {@code recipient}, {@code channel}, {@code subject}, {@code body}
     * @return the delivery outcome
     */
    @PostMapping("/notify")
    public NotificationResult notify(@RequestBody Map<String, String> request) {
        NotificationMessage message = NotificationMessage.builder()
            .recipient(request.getOrDefault("recipient", "user@example.com"))
            .channel(Channel.valueOf(request.getOrDefault("channel", "EMAIL")))
            .subject(request.get("subject"))
            .body(request.get("body"))
            .build();
        return notifications.send(message);
    }
}
