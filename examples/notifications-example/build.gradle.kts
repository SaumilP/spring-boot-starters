plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-notifications usage"

dependencies {
    implementation(project(":spring-boot-starter-notifications"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
