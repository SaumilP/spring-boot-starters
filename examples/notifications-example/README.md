# notifications-example

Demonstrates [`spring-boot-starter-notifications`](../../spring-boot-starter-notifications/README.md):
sending a message through the `NotificationService` façade. With no provider on the classpath, the
built-in logging sender handles every channel, so it runs with no external setup.

## Run

```bash
./gradlew :examples:notifications-example:bootRun
```

## Try it

```bash
curl -s -X POST http://localhost:8080/notify \
  -H 'Content-Type: application/json' \
  -d '{"recipient":"ada@example.com","channel":"EMAIL","subject":"Hi","body":"Welcome!"}'
# → {"success":true,"channel":"EMAIL","providerMessageId":"<uuid>","detail":null}
```

Watch the application log for the `[notifications]` line the logging sender emits. Add a provider
starter (e.g. Resend, Twilio) and its `NotificationSender` is picked up automatically — the same
call routes to the real transport.
