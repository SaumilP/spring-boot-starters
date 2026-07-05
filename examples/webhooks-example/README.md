# webhooks-example

Demonstrates [`spring-boot-starter-webhooks`](../../spring-boot-starter-webhooks/README.md):
registering a subscriber, then delivering a signed event to it with retry and dead-lettering. The
app hosts its own `/receive` endpoint so the full loop runs locally with no external setup.

## Run

```bash
./gradlew :examples:webhooks-example:bootRun
```

## Try it

```bash
# Subscribe the app's own /receive endpoint
curl -s -X POST http://localhost:8080/subscribe \
  -H 'Content-Type: application/json' \
  -d '{"id":"local","url":"http://localhost:8080/receive","secret":"topsecret"}'
# → {"status":"subscribed","id":"local"}

# Emit an event — it is signed and POSTed to every active subscriber
curl -s -X POST http://localhost:8080/emit \
  -H 'Content-Type: application/json' \
  -d '{"type":"order.created","payload":"{\"id\":42}"}'
# → [{"endpointId":"local","delivered":true,"attempts":1,"detail":null}]
```

The receiver logs the `X-Webhook-Signature` header; subscribers verify authenticity by recomputing
`HmacSHA256(body, secret)`. Point a subscriber at an unreachable URL to watch the retry/backoff and
dead-lettering behaviour in the logs.
