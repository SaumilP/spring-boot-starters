plugins {
    id("org.springframework.boot") apply false
}

description = "Spring Boot Starter - PII protection: log/value masking and JPA field-level AES-GCM encryption"
version     = "1.0.0"

dependencies {
    api(project(":spring-boot-starter-common"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("jakarta.persistence:jakarta.persistence-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.javadoc {
    source = sourceSets["main"].allJava
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}
