# spring-boot-starter-llm-client

A Spring Boot starter that provides a production-ready, OpenAI-compatible LLM client with
automatic retry, Micrometer metrics, and zero boilerplate configuration. Works with OpenAI,
Azure OpenAI, Ollama, and any endpoint that follows the OpenAI Chat Completions API.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.saumilp.starters/spring-boot-starter-llm-client.svg)](https://central.sonatype.com/search?q=io.github.saumilp.starters)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## Overview

Integrating an LLM into a Spring Boot application typically involves wiring a REST client,
handling authentication headers, implementing retry logic, and adding observability. This
starter does all of that for you — just inject `LlmClient` and call `ask()`.

The `LlmClient` interface is provider-agnostic. The bundled `RestClientLlmClient` uses
Spring 6's `RestClient` and speaks the OpenAI Chat Completions API, which is also implemented
by Ollama, Azure OpenAI, and many self-hosted models.

---

## Features

- **`LlmClient` interface** — clean abstraction injectable anywhere in your Spring context
- **`RestClient`-based implementation** — uses Spring 6's modern HTTP client, no additional dependencies
- **Retry with linear back-off** — configurable retry count; fail-fast or resilient depending on your needs
- **Micrometer metrics** — `llm.chat.duration` timer with `model` and `outcome` tags; activates only when Micrometer is present
- **Model substitution** — set a global default model; override per-request as needed
- **`@ConditionalOnMissingBean`** throughout — replace any bean without touching the starter
- **Ollama, Azure OpenAI, and self-hosted** endpoints supported via `base-url` configuration

---

## Requirements

| Dependency        | Version    |
|-------------------|------------|
| Java              | 21+        |
| Spring Boot       | 4.0.4+     |
| LLM endpoint      | Any OpenAI-compatible (OpenAI, Azure, Ollama, etc.) |
| Micrometer (opt.) | Any version compatible with Spring Boot 4.x |

---

## Installation

**Gradle:**
```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-llm-client:1.0.0'
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.saumilp.starters</groupId>
    <artifactId>spring-boot-starter-llm-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start

### 1. Configure your API key and model

```yaml
spring:
  llm:
    base-url: https://api.openai.com
    api-key: ${OPENAI_API_KEY}
    default-model: gpt-4o-mini
```

### 2. Inject and use `LlmClient`

```java
@Service
public class SummaryService {

    private final LlmClient llmClient;

    public SummaryService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public String summarise(String text) {
        return llmClient.ask("Summarise the following in two sentences:\n\n" + text);
    }
}
```

### 3. Multi-turn conversation

```java
List<ChatMessage> messages = List.of(
    ChatMessage.system("You are a concise technical writer."),
    ChatMessage.user("Explain idempotency in one paragraph.")
);

ChatResponse response = llmClient.chat(ChatRequest.of("gpt-4o", messages));
System.out.println(response.firstContent());
// Usage stats
response.usage().totalTokens(); // 312
```

---

## Configuration Reference

All properties are under the `spring.llm` prefix.

| Property                   | Type      | Default                      | Description |
|----------------------------|-----------|------------------------------|-------------|
| `enabled`                  | `boolean` | `true`                       | Set to `false` to disable auto-configuration entirely |
| `base-url`                 | `String`  | `https://api.openai.com`     | Base URL of the OpenAI-compatible endpoint (no trailing slash) |
| `api-key`                  | `String`  | `""`                         | API key sent in `Authorization: Bearer` header |
| `default-model`            | `String`  | `gpt-4o-mini`                | Model used by `LlmClient.ask()` and when request model is `"default"` |
| `max-retries`              | `int`     | `3`                          | Retry attempts on `RestClientException`; `0` disables retries |
| `connect-timeout-seconds`  | `int`     | `10`                         | HTTP connection timeout |
| `read-timeout-seconds`     | `int`     | `60`                         | HTTP read timeout — set high enough for slow model inference |

### Full annotated `application.yml`

```yaml
spring:
  llm:
    enabled: true
    base-url: https://api.openai.com   # override for local/Azure
    api-key: ${OPENAI_API_KEY}          # inject from environment
    default-model: gpt-4o-mini          # used by ask() and unspecified chat() calls
    max-retries: 3                       # 0 = no retries
    connect-timeout-seconds: 10
    read-timeout-seconds: 60
```

---

## Using with Ollama (local models)

```yaml
spring:
  llm:
    base-url: http://localhost:11434
    api-key: ollama           # Ollama ignores the key; any non-empty value works
    default-model: llama3.2
    read-timeout-seconds: 120  # local inference can be slower
```

Start Ollama and pull the model:
```bash
ollama pull llama3.2
ollama serve
```

---

## Using with Azure OpenAI

```yaml
spring:
  llm:
    base-url: https://<resource-name>.openai.azure.com/openai/deployments/<deployment-name>
    api-key: ${AZURE_OPENAI_API_KEY}
    default-model: gpt-4o        # must match your Azure deployment name
```

> **Note:** Azure OpenAI uses a slightly different URL structure. The path
> `/v1/chat/completions` is appended automatically by the starter.

---

## Metrics

When `spring-boot-starter-actuator` and Micrometer are on the classpath, the starter
records a `llm.chat.duration` timer for every `chat()` call:

| Tag       | Values              |
|-----------|---------------------|
| `model`   | The model name used |
| `outcome` | `success`, `failure` |

Prometheus output example:
```
llm_chat_duration_seconds_count{model="gpt-4o-mini",outcome="success"} 42
llm_chat_duration_seconds_sum{model="gpt-4o-mini",outcome="success"}   18.3
```

---

## Overriding Beans

All beans use `@ConditionalOnMissingBean`. To replace the client entirely:

```java
@Bean
public LlmClient customLlmClient() {
    return request -> { /* your implementation */ };
}
```

To use a different HTTP client with custom SSL configuration:

```java
@Bean(name = "llmRestClient")
public RestClient llmRestClient() {
    return RestClient.builder()
        .baseUrl("https://my-proxy.internal")
        .requestInterceptor(new MyAuthInterceptor())
        .build();
}
```

---

## Supported Versions

| Spring Boot | Java | Status   |
|-------------|------|----------|
| 4.0.x       | 21   | ✅ Supported |

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for coding standards, Javadoc requirements,
and how to add a new starter.

---

[GitHub](https://github.com/SaumilP/spring-boot-starters) · [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
