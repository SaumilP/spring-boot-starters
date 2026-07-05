plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-multitenancy usage"

dependencies {
    implementation(project(":spring-boot-starter-multitenancy"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
