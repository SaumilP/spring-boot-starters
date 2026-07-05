/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.securityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example application demonstrating {@code spring-boot-starter-security-jwt}: a public endpoint that
 * bypasses authentication and a secured endpoint that requires a valid bearer token, with secure
 * response headers applied by the opinionated filter chain.
 *
 * @author SaumilP
 */
@SpringBootApplication
@RestController
public class SecurityJwtExampleApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SecurityJwtExampleApplication.class, args);
    }

    /**
     * A public endpoint (listed in {@code spring.security-jwt.public-paths}).
     *
     * @return a greeting
     */
    @GetMapping("/public/ping")
    public String publicPing() {
        return "pong";
    }

    /**
     * A secured endpoint requiring a valid JWT.
     *
     * @return a greeting
     */
    @GetMapping("/secure/hello")
    public String secureHello() {
        return "hello, authenticated caller";
    }
}
