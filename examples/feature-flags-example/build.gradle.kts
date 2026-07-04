plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-feature-flags usage"

dependencies {
    implementation(project(":spring-boot-starter-feature-flags"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
}
