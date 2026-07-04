plugins {
    id("org.springframework.boot") apply false
}

description = "Spring Boot Starter - Annotation-driven audit logging with pluggable sinks (log, JPA, composite)"
version     = "1.0.0"

dependencies {
    api(project(":spring-boot-starter-common"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("io.micrometer:micrometer-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-aop")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.javadoc {
    source = sourceSets["main"].allJava
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}
