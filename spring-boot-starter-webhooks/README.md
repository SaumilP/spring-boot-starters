# spring-boot-starter-webhooks

**Outbound webhook delivery** — one dependency gives you a signed, retrying, dead-lettered delivery
pipeline for pushing events to subscriber URLs. Pairs naturally with
[`spring-boot-starter-outbox`](../spring-boot-starter-outbox/README.md): drain the outbox and hand
each event to `WebhookDeliveryService`.

Each request is HMAC-signed so subscribers can verify authenticity, retried with exponential
backoff on transient failures, and recorded in a dead-letter store when all attempts are exhausted.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-webhooks:1.0.0'
}
```

---

## What you get

| Bean | Purpose |
|---|---|
| `WebhookDeliveryService` | Signs, POSTs, retries, and dead-letters events |
| `WebhookRegistry` (`InMemoryWebhookRegistry`) | Stores subscriber endpoints |
| `WebhookSigner` | HMAC-signs payloads (`sha256=...`) |
| `DeadLetterStore` (`InMemoryDeadLetterStore`) | Records exhausted deliveries |
| `webhookRestClient` (`RestClient`) | Timeout-configured HTTP client |

All beans are `@ConditionalOnMissingBean`; supply your own (e.g. a JPA-backed `WebhookRegistry`) to
override.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.webhooks.enabled` | `true` | Master switch |
| `spring.webhooks.signature-header` | `X-Webhook-Signature` | Header carrying the HMAC |
| `spring.webhooks.signature-algorithm` | `HmacSHA256` | JCA `Mac` algorithm |
| `spring.webhooks.connect-timeout` | `2s` | Connection timeout |
| `spring.webhooks.read-timeout` | `5s` | Read timeout |
| `spring.webhooks.retry.max-attempts` | `3` | Max delivery attempts |
| `spring.webhooks.retry.backoff` | `200ms` | Base backoff |
| `spring.webhooks.retry.multiplier` | `2.0` | Exponential multiplier |
| `spring.webhooks.dead-letter.enabled` | `true` | Record exhausted deliveries |

---

## Usage

```java
@Service
class OrderEvents {

    private final WebhookRegistry registry;
    private final WebhookDeliveryService delivery;

    OrderEvents(WebhookRegistry registry, WebhookDeliveryService delivery) {
        this.registry = registry;
        this.delivery = delivery;
    }

    void subscribe(String id, String url, String secret) {
        registry.register(WebhookEndpoint.active(id, url, secret));
    }

    void orderCreated(String json) {
        delivery.deliverToAll(WebhookEvent.of("order.created", json));
    }
}
```

Each POST carries `X-Webhook-Signature`, `X-Webhook-Id`, and `X-Webhook-Event`. Subscribers verify
the signature by recomputing `HmacSHA256(body, secret)` and comparing against the header.

- `4xx` responses are treated as **permanent** failures (not retried).
- Connection errors and `5xx` responses are **retried** up to `retry.max-attempts`.
- Exhausted deliveries land in the `DeadLetterStore` for inspection or replay.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
