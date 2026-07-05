/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.source;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AwsSecretsManagerSecretSource}.
 */
class AwsSecretsManagerSecretSourceTest {

    private final SecretsManagerClient client = mock(SecretsManagerClient.class);
    private final AwsSecretsManagerSecretSource source = new AwsSecretsManagerSecretSource(client);

    @Test
    void should_returnSecret_when_found() {
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
            .thenReturn(GetSecretValueResponse.builder().secretString("token-123").build());

        assertThat(source.get("api/token")).contains("token-123");
    }

    @Test
    void should_returnEmpty_when_secretNotFound() {
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
            .thenThrow(ResourceNotFoundException.builder().message("missing").build());

        assertThat(source.get("api/absent")).isEmpty();
    }
}
