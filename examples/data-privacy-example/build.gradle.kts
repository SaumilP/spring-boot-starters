plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-data-privacy usage"

dependencies {
    implementation(project(":spring-boot-starter-data-privacy"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}
