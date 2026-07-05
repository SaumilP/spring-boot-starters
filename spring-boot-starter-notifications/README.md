# spring-boot-starter-notifications

The **core notification SPI** — one API for sending email, SMS, push, in-app, and WhatsApp
messages, with the actual transport supplied by pluggable provider senders.

Inject a single `NotificationService`, build a `NotificationMessage`, and send it. A
`CompositeNotificationSender` routes each message to the first registered `NotificationSender` that
supports its channel — mirroring the "common interface, swappable provider" pattern this repo uses
for storage (`minio` / `aws-s3`) and audit sinks.

Provider starters (Twilio, Resend, OneSignal, Novu, ...) register a `NotificationSender` for the
channels they support and are picked up automatically. With no provider on the classpath, a
`LoggingNotificationSender` handles every channel so the starter is usable out of the box.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-notifications:1.0.0'
}
```

---

## What you get

| Bean | Condition | Purpose |
|---|---|---|
| `NotificationService` | always | Façade with synchronous `send` and async `sendAsync` |
| `CompositeNotificationSender` | always | Routes a message to the delegate that supports its channel |
| `LoggingNotificationSender` | no provider / `logging-sender.enabled=true` | Fallback that logs instead of delivering |
| `notificationExecutor` (`Executor`) | always | Backs `sendAsync` |

All beans are `@ConditionalOnMissingBean`; declare your own to override.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.notifications.enabled` | `true` | Master switch |
| `spring.notifications.default-channel` | `EMAIL` | Channel assumed when unspecified |
| `spring.notifications.logging-sender.enabled` | `true` | Register the logging fallback sender |

---

## Usage

```java
@Service
class WelcomeService {

    private final NotificationService notifications;

    WelcomeService(NotificationService notifications) {
        this.notifications = notifications;
    }

    void welcome(String email) {
        var message = NotificationMessage.builder()
            .recipient(email)
            .channel(Channel.EMAIL)
            .subject("Welcome!")
            .body("Thanks for signing up.")
            .build();
        NotificationResult result = notifications.send(message);
        if (!result.success()) {
            // handle result.detail()
        }
    }
}
```

### Implementing a provider

Register a bean implementing `NotificationSender`:

```java
@Bean
NotificationSender smsSender(SmsGateway gateway) {
    return new NotificationSender() {
        @Override public boolean supports(Channel channel) { return channel == Channel.SMS; }
        @Override public NotificationResult send(NotificationMessage m) {
            String id = gateway.send(m.recipient(), m.body());
            return NotificationResult.success(Channel.SMS, id);
        }
    };
}
```

The composite picks it up automatically and routes `SMS` messages to it.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
