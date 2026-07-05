/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.notifications.config;

import io.github.saumilp.starters.notifications.sender.CompositeNotificationSender;
import io.github.saumilp.starters.notifications.sender.LoggingNotificationSender;
import io.github.saumilp.starters.notifications.service.NotificationService;
import io.github.saumilp.starters.notifications.spi.NotificationSender;
import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Auto-configuration for the notifications core SPI.
 *
 * <p>Registers a {@link LoggingNotificationSender} fallback (unless disabled), a
 * {@link CompositeNotificationSender} that routes across every {@link NotificationSender} bean, an
 * async {@link Executor}, and the {@link NotificationService} façade. Provider starters contribute
 * their own {@link NotificationSender} beans, which are picked up automatically by the composite.
 *
 * @author SaumilP
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(NotificationsProperties.class)
@ConditionalOnProperty(prefix = "spring.notifications", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class NotificationsAutoConfiguration {

    /** Creates the notifications auto-configuration. */
    public NotificationsAutoConfiguration() {
    }

    /**
     * Registers the logging fallback sender when enabled.
     *
     * @return a {@link LoggingNotificationSender}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(LoggingNotificationSender.class)
    @ConditionalOnProperty(prefix = "spring.notifications.logging-sender", name = "enabled",
        havingValue = "true", matchIfMissing = true)
    public LoggingNotificationSender loggingNotificationSender() {
        return new LoggingNotificationSender();
    }

    /**
     * Registers the composite sender over all discovered {@link NotificationSender} beans.
     *
     * @param senders all sender beans in the context (excluding the composite itself)
     * @return a {@link CompositeNotificationSender}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(CompositeNotificationSender.class)
    public CompositeNotificationSender compositeNotificationSender(List<NotificationSender> senders) {
        return new CompositeNotificationSender(senders);
    }

    /**
     * Registers the async executor used by {@link NotificationService#sendAsync}.
     *
     * @return an {@link Executor}; never {@code null}
     */
    @Bean("notificationExecutor")
    @ConditionalOnMissingBean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        return new SimpleAsyncTaskExecutor("notifications-");
    }

    /**
     * Registers the {@link NotificationService} façade.
     *
     * @param sender             the composite sender; must not be {@code null}
     * @param notificationExecutor the async executor; must not be {@code null}
     * @return a {@link NotificationService}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    public NotificationService notificationService(CompositeNotificationSender sender,
                                                   Executor notificationExecutor) {
        return new NotificationService(sender, notificationExecutor);
    }
}
