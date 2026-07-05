/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.source;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.util.Optional;

/**
 * {@link SecretSource} backed by AWS Secrets Manager (AWS SDK v2).
 *
 * <p>Each {@link #get(String)} performs a {@code GetSecretValue} call; a missing secret is returned
 * as {@link Optional#empty()} rather than raising an exception.
 *
 * @since 1.0.0
 */
public class AwsSecretsManagerSecretSource implements SecretSource {

    private final SecretsManagerClient client;

    /**
     * Constructs the source.
     *
     * @param client the AWS Secrets Manager client; must not be {@code null}
     */
    public AwsSecretsManagerSecretSource(SecretsManagerClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     *
     * @param name the secret id or ARN; must not be {@code null}
     * @return the secret string, or {@link Optional#empty()} if the secret does not exist
     */
    @Override
    public Optional<String> get(String name) {
        try {
            GetSecretValueResponse response = client.getSecretValue(
                GetSecretValueRequest.builder().secretId(name).build());
            return Optional.ofNullable(response.secretString());
        } catch (ResourceNotFoundException ex) {
            return Optional.empty();
        }
    }
}
