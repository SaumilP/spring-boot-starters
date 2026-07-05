/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.saumilp.starters.notifications.sender.CompositeNotificationSender;
import io.github.saumilp.starters.notifications.sender.LoggingNotificationSender;
import io.github.saumilp.starters.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotificationsAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(NotificationsAutoConfiguration.class));

    @Test
    void should_registerCoreBeans_when_default() {
        runner.run(context -> assertThat(context)
            .hasSingleBean(LoggingNotificationSender.class)
            .hasSingleBean(CompositeNotificationSender.class)
            .hasSingleBean(NotificationService.class));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.notifications.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(NotificationService.class)
                .doesNotHaveBean(CompositeNotificationSender.class));
    }

    @Test
    void should_omitLoggingSender_when_loggingSenderDisabled() {
        runner.withPropertyValues("spring.notifications.logging-sender.enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(LoggingNotificationSender.class)
                .hasSingleBean(CompositeNotificationSender.class));
    }
}
