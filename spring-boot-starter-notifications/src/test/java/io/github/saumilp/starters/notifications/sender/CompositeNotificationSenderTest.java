/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.sender;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.notifications.model.Channel;
import io.github.saumilp.starters.notifications.model.NotificationMessage;
import io.github.saumilp.starters.notifications.model.NotificationResult;
import io.github.saumilp.starters.notifications.spi.NotificationSender;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeNotificationSenderTest {

    private static NotificationSender senderFor(Channel supported) {
        return new NotificationSender() {
            @Override
            public boolean supports(Channel channel) {
                return channel == supported;
            }

            @Override
            public NotificationResult send(NotificationMessage message) {
                return NotificationResult.success(message.channel(), supported.name() + "-1");
            }
        };
    }

    @Test
    void should_routeToSupportingDelegate_when_channelMatches() {
        var composite = new CompositeNotificationSender(
            List.of(senderFor(Channel.SMS), senderFor(Channel.EMAIL)));

        var result = composite.send(NotificationMessage.builder()
            .recipient("a@b.com").channel(Channel.EMAIL).body("hi").build());

        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).isEqualTo("EMAIL-1");
    }

    @Test
    void should_returnFailure_when_noDelegateSupportsChannel() {
        var composite = new CompositeNotificationSender(List.of(senderFor(Channel.EMAIL)));

        var result = composite.send(NotificationMessage.builder()
            .recipient("+15550001111").channel(Channel.SMS).body("hi").build());

        assertThat(result.success()).isFalse();
        assertThat(result.detail()).contains("SMS");
    }

    @Test
    void should_reportSupported_when_anyDelegateSupports() {
        var composite = new CompositeNotificationSender(List.of(senderFor(Channel.PUSH)));

        assertThat(composite.supports(Channel.PUSH)).isTrue();
        assertThat(composite.supports(Channel.WHATSAPP)).isFalse();
        assertThat(composite.delegateCount()).isEqualTo(1);
    }
}
