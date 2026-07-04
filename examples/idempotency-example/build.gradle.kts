plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-idempotency usage"

dependencies {
    implementation(project(":spring-boot-starter-idempotency"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
