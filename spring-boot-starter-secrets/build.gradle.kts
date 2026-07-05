plugins {
    id("org.springframework.boot") apply false
}

description = "Spring Boot Starter - Unified SecretSource over environment and AWS Secrets Manager"
version     = "1.0.0"

dependencies {
    api(project(":spring-boot-starter-common"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation(platform("software.amazon.awssdk:bom:2.26.27"))
    compileOnly("software.amazon.awssdk:secretsmanager")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("software.amazon.awssdk:bom:2.26.27"))
    testImplementation("software.amazon.awssdk:secretsmanager")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.javadoc {
    source = sourceSets["main"].allJava
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}
