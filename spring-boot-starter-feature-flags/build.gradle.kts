plugins {
    id("org.springframework.boot") apply false
}

description = "Spring Boot Starter - OpenFeature-based feature flag evaluation with file-based and Unleash providers"
version     = "1.0.0"

dependencies {
    api(project(":spring-boot-starter-common"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("dev.openfeature:sdk:1.11.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.javadoc {
    source = sourceSets["main"].allJava
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}
