/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.apikeys;

import io.github.saumilp.starters.apikeys.model.ApiKey;
import io.github.saumilp.starters.apikeys.model.IssuedApiKey;
import io.github.saumilp.starters.apikeys.service.ApiKeyService;
import io.github.saumilp.starters.apikeys.web.ApiKeyAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Issues API keys and exposes a protected endpoint guarded by the {@code X-Api-Key} filter.
 *
 * @author SaumilP
 */
@RestController
public class ApiKeyController {

    private final ApiKeyService apiKeys;

    /**
     * Creates the controller.
     *
     * @param apiKeys the key service; must not be {@code null}
     */
    public ApiKeyController(ApiKeyService apiKeys) {
        this.apiKeys = apiKeys;
    }

    /**
     * Issues a new key for the given principal (public path).
     *
     * @param body a JSON body with a {@code principal}
     * @return the one-time plaintext key and its id
     */
    @PostMapping("/keys")
    public Map<String, String> issue(@RequestBody Map<String, String> body) {
        IssuedApiKey issued = apiKeys.issue(
            body.getOrDefault("principal", "anonymous"), Set.of("read"));
        return Map.of("apiKey", issued.plaintext(), "id", issued.apiKey().id());
    }

    /**
     * A protected endpoint — requires a valid {@code X-Api-Key} header.
     *
     * @param request the current request (carries the resolved principal attribute)
     * @return a greeting naming the authenticated principal
     */
    @GetMapping("/internal/data")
    public Map<String, String> data(HttpServletRequest request) {
        ApiKey key = (ApiKey) request.getAttribute(ApiKeyAuthFilter.PRINCIPAL_ATTRIBUTE);
        return Map.of("data", "secret", "principal", key.principal());
    }
}
