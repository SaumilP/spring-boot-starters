# llm-client-example

Demonstrates the `spring-boot-starter-llm-client` starter with a simple chat API backed by a local Ollama instance.

## Prerequisites

- Java 21
- Docker and Docker Compose

## Running locally

**1. Start Ollama:**
```bash
docker compose up -d
```

**2. Pull a model:**
```bash
docker exec ollama ollama pull llama3.2
```

**3. Run the application:**
```bash
./gradlew :examples:llm-client-example:bootRun
```

## API

### Send a message
```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is the capital of France?"}'
# {"message":"What is the capital of France?","response":"The capital of France is Paris."}
```

## Using OpenAI instead of Ollama

Update `application.yml`:
```yaml
spring:
  llm:
    base-url: https://api.openai.com
    api-key: sk-...
    default-model: gpt-4o-mini
```

## Using Azure OpenAI

```yaml
spring:
  llm:
    base-url: https://<resource>.openai.azure.com/openai/deployments/<deployment>
    api-key: <azure-api-key>
    default-model: gpt-4o
```
