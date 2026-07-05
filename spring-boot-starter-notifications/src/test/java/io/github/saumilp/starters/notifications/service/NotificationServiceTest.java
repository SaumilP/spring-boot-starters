/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.sender.CompositeNotificationSender;
import io.github.saumilp.starters.notifications.sender.LoggingNotificationSender;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private final NotificationService service = new NotificationService(
        new CompositeNotificationSender(List.of(new LoggingNotificationSender())),
        Runnable::run);

    private static NotificationMessage message() {
        return NotificationMessage.builder()
            .recipient("a@b.com").channel(Channel.EMAIL).subject("Hi").body("Body").build();
    }

    @Test
    void should_sendSynchronously() {
        var result = service.send(message());
        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).isNotBlank();
    }

    @Test
    void should_sendAsynchronously() throws ExecutionException, InterruptedException {
        var result = service.sendAsync(message()).get();
        assertThat(result.success()).isTrue();
    }

    @Test
    void should_rejectBlankRecipient() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> NotificationMessage.builder().recipient(" ").channel(Channel.EMAIL).build()))
            .hasMessageContaining("recipient");
    }
}
