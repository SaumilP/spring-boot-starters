/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.examples.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates the spring-boot-starter-llm-client starter with a simple chat REST API backed by Ollama.
 */
@SpringBootApplication
public class LlmClientExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmClientExampleApplication.class, args);
    }
}
