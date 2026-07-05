# spring-boot-starter-scheduler-lock

Ensure a `@Scheduled` task runs on **exactly one instance** across a cluster — annotate the method
and pick a lock provider.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-scheduler-lock:1.0.0'
}
```

---

## Usage

```java
@Scheduled(fixedRate = 60_000)
@SchedulerLock(name = "nightly-report", lockAtMostFor = "PT5M")
public void generateReport() {
    // runs on one instance per fire; others skip
}
```

The aspect acquires the named lock before the method runs and releases it afterwards. If another
instance already holds the lock, the run is skipped. `lockAtMostFor` (ISO-8601) is a safety ceiling
that auto-releases the lock even if the holding instance crashes.

---

## Providers

| `spring.scheduler-lock.provider` | Bean | Cluster-safe | Requires |
|---|---|---|---|
| `in-memory` (default) | `InMemoryLockProvider` | No (single JVM) | — |
| `redis` | `RedisLockProvider` | Yes | `spring-boot-starter-data-redis` + a `StringRedisTemplate` |

The Redis provider uses an atomic `SET key value NX PX ttl`. Declare your own `LockProvider` bean to
plug in a different backend (e.g. JDBC).

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.scheduler-lock.enabled` | `true` | Master switch |
| `spring.scheduler-lock.provider` | `in-memory` | `in-memory` or `redis` |
| `spring.scheduler-lock.default-lock-at-most-for` | `1m` | Default hold ceiling when the annotation omits `lockAtMostFor` |
| `spring.scheduler-lock.redis-key-prefix` | `scheduler-lock:` | Key namespace for the Redis provider |

---

## Notes

- Only `lockAtMostFor` (max hold) is implemented; `lockAtLeastFor` (minimum hold to avoid rapid
  re-runs) is a planned enhancement.
- The lock is released as soon as the task completes, so `lockAtMostFor` normally only matters if
  the holder crashes mid-run.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
