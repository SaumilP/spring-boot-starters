plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "spring-boot-starters"

include(
    "spring-boot-starter-common",
    "spring-boot-starter-minio",
    "spring-boot-starter-redis",
    "spring-boot-starter-rate-limiting",
    "spring-boot-starter-idempotency",
    "spring-boot-starter-audit-log",
    "spring-boot-starter-feature-flags",
    "spring-boot-starter-llm-client",
    "spring-boot-starter-multitenancy",
    "spring-boot-starter-aws-s3",
    "spring-boot-starter-outbox",
    "spring-boot-starter-observability",
    "spring-boot-starter-problem-details",
    "spring-boot-starter-resilient-client",
    "spring-boot-starter-data-privacy",
    "spring-boot-starter-scheduler-lock",
    "spring-boot-starter-secrets",
    "spring-boot-starter-notifications",
    "spring-boot-starter-security-jwt",
    "spring-boot-starter-webhooks",
    "spring-boot-starter-api-keys",
    "examples:redis-example",
    "examples:minio-example",
    "examples:rate-limiting-example",
    "examples:idempotency-example",
    "examples:audit-log-example",
    "examples:feature-flags-example",
    "examples:llm-client-example",
    "examples:multitenancy-example",
    "examples:aws-s3-example",
    "examples:outbox-example",
    "examples:data-privacy-example",
    "examples:notifications-example",
    "examples:security-jwt-example",
    "examples:webhooks-example",
    "examples:api-keys-example"
)
