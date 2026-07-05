plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-api-keys usage"

dependencies {
    implementation(project(":spring-boot-starter-api-keys"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
