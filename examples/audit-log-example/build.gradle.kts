plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Example: spring-boot-starter-audit-log usage"

dependencies {
    implementation(project(":spring-boot-starter-audit-log"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-aop")
    implementation("org.aspectj:aspectjweaver")
}
