plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-webhooks usage"

dependencies {
    implementation(project(":spring-boot-starter-webhooks"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
