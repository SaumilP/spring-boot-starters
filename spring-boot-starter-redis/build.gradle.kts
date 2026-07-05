plugins {
    id("org.springframework.boot") apply false
}

description = "Spring Boot Starter - Redis utilities, distributed locking, health indicators and Micrometer metrics"
version     = "1.0.0"

dependencies {
    api(project(":spring-boot-starter-common"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("io.micrometer:micrometer-core")
    compileOnly("org.springframework:spring-aop")
    compileOnly("org.aspectj:aspectjweaver")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("io.micrometer:micrometer-core")
    testImplementation("org.aspectj:aspectjweaver")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.javadoc {
    source = sourceSets["main"].allJava
    // RedisUtil and RedisLockUtil are internal helpers that wrap the low-level, byte[]-based
    // RedisConnection API. That API is deprecated/raw-typed upstream, which produces
    // unchecked/deprecation compiler notes the Javadoc tool surfaces but cannot be told to
    // suppress. They are excluded from generated Javadoc (runtime code is untouched).
    exclude("**/utils/RedisUtil.java", "**/utils/RedisLockUtil.java")
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}
