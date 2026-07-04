plugins {
    id("org.springframework.boot")        version "4.0.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless")           version "7.0.3" apply false
}

group = "io.github.saumilp.starters"

subprojects {
    val isExample = project.path.startsWith(":examples")

    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    if (!isExample) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        if (!isExample) {
            withJavadocJar()
            withSourcesJar()
        }
    }

    repositories {
        mavenCentral()
    }

    // Starters: use Gradle-native BOM platform so version resolution works without
    // io.spring.dependency-management being applied via subprojects block.
    // Examples: apply io.spring.dependency-management in their own plugins block,
    // which auto-imports the Spring Boot BOM when org.springframework.boot is also applied.
    if (!isExample) {
        dependencies {
            val springBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            "implementation"(springBom)
            "compileOnly"(springBom)
            "annotationProcessor"(springBom)
            "testImplementation"(springBom)
            "testRuntimeOnly"(springBom)
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            trimTrailingWhitespace()
            leadingTabsToSpaces()
            endWithNewline()
            removeUnusedImports()
        }
        format("misc") {
            target("*.gradle.kts", "*.md", ".gitignore")
            trimTrailingWhitespace()
            leadingTabsToSpaces()
            endWithNewline()
        }
    }

    if (!isExample) {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    pom {
                        url.set("https://github.com/SaumilP/spring-boot-starters")
                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("SaumilP")
                                name.set("SaumilP")
                                email.set("email2saumil2024@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/SaumilP/spring-boot-starters.git")
                            developerConnection.set("scm:git:ssh://github.com/SaumilP/spring-boot-starters.git")
                            url.set("https://github.com/SaumilP/spring-boot-starters")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "OSSRH"
                    url = uri(
                        if (version.toString().endsWith("SNAPSHOT"))
                            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        else
                            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    )
                    credentials {
                        username = (findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")) as String?
                        password = (findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")) as String?
                    }
                }
            }
        }

        configure<SigningExtension> {
            val key  = (findProperty("signingKey")      ?: System.getenv("GPG_PRIVATE_KEY"))  as String?
            val pass = (findProperty("signingPassword") ?: System.getenv("GPG_PASSPHRASE"))    as String?
            if (key != null) {
                useInMemoryPgpKeys(key, pass)
                sign(extensions.getByType<PublishingExtension>().publications["mavenJava"])
            }
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }
}
