/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.client;

import io.github.saumilp.starters.llm.config.LlmClientProperties;
import io.github.saumilp.starters.llm.exception.LlmClientException;
import io.github.saumilp.starters.llm.model.ChatRequest;
import io.github.saumilp.starters.llm.model.ChatResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link LlmClient} implementation using Spring 6's {@link RestClient} to communicate
 * with any OpenAI-compatible chat completions endpoint.
 *
 * <h2>Retry behaviour</h2>
 * <p>On a {@link RestClientException}, the request is retried up to
 * {@link LlmClientProperties#getMaxRetries()} times with a linear back-off of
 * {@code 500 ms × attempt}. After all attempts are exhausted, a
 * {@link LlmClientException} is thrown wrapping the last exception.
 *
 * <h2>Metrics</h2>
 * <p>When a {@link MeterRegistry} is injected (i.e., Micrometer is on the classpath and
 * an actuator metrics endpoint is configured), each call is timed and recorded under
 * the {@code llm.chat.duration} metric with a {@code model} tag. If no registry is
 * provided, metrics are silently skipped.
 *
 * @since 1.0.0
 */
public class RestClientLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientLlmClient.class);
    private static final String COMPLETIONS_PATH = "/v1/chat/completions";

    private final RestClient restClient;
    private final LlmClientProperties props;
    private final MeterRegistry meterRegistry;

    /**
     * Constructs a {@code RestClientLlmClient}.
     *
     * @param restClient    a pre-configured {@link RestClient} with the base URL set;
     *                      must not be {@code null}
     * @param props         the starter configuration properties; must not be {@code null}
     * @param meterRegistry optional Micrometer registry for timing metrics;
     *                      may be {@code null} to disable metrics
     */
    public RestClientLlmClient(RestClient restClient,
                                LlmClientProperties props,
                                MeterRegistry meterRegistry) {
        this.restClient    = restClient;
        this.props         = props;
        this.meterRegistry = meterRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Substitutes {@link LlmClientProperties#getDefaultModel()} when the request's model
     * field is {@code "default"} or blank. Sends a POST to {@code /v1/chat/completions} with
     * an {@code Authorization: Bearer <apiKey>} header and a JSON body matching the OpenAI
     * schema.
     *
     * @param request the chat request; must not be {@code null}
     * @return the parsed {@link ChatResponse}; never {@code null}
     * @throws LlmClientException if all retry attempts fail
     */
    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = resolveModel(request);
        Timer.Sample sample = startTimer();

        RestClientException lastException = null;
        for (int attempt = 0; attempt <= props.getMaxRetries(); attempt++) {
            if (attempt > 0) {
                backOff(attempt);
            }
            try {
                ChatResponse response = doPost(request, model);
                recordSuccess(sample, model);
                return response;
            } catch (RestClientException ex) {
                lastException = ex;
                log.warn("LLM request failed (attempt {}/{}): {}", attempt + 1,
                    props.getMaxRetries() + 1, ex.getMessage());
            }
        }
        recordFailure(sample, model);
        throw new LlmClientException("LLM request failed after " + (props.getMaxRetries() + 1)
            + " attempts", lastException);
    }

    private ChatResponse doPost(ChatRequest request, String model) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", request.messages());
        body.put("temperature", request.temperature());
        body.put("max_tokens", request.maxTokens());

        return restClient.post()
            .uri(COMPLETIONS_PATH)
            .header("Authorization", "Bearer " + props.getApiKey())
            .header("Content-Type", "application/json")
            .body(body)
            .retrieve()
            .body(ChatResponse.class);
    }

    private String resolveModel(ChatRequest request) {
        String model = request.model();
        if ("default".equals(model) || model.isBlank()) {
            return props.getDefaultModel();
        }
        return model;
    }

    private void backOff(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LlmClientException("Interrupted during retry back-off", ie);
        }
    }

    private Timer.Sample startTimer() {
        if (meterRegistry == null) return null;
        return Timer.start(meterRegistry);
    }

    private void recordSuccess(Timer.Sample sample, String model) {
        if (sample == null || meterRegistry == null) return;
        sample.stop(Timer.builder("llm.chat.duration")
            .tag("model", model)
            .tag("outcome", "success")
            .register(meterRegistry));
    }

    private void recordFailure(Timer.Sample sample, String model) {
        if (sample == null || meterRegistry == null) return;
        sample.stop(Timer.builder("llm.chat.duration")
            .tag("model", model)
            .tag("outcome", "failure")
            .register(meterRegistry));
    }
}
