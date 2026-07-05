/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.secrets.source;

import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * {@link SecretSource} backed by the Spring {@link Environment}.
 *
 * <p>Resolves secrets from any configured property source (environment variables, system
 * properties, config files). This is the default provider and requires no external service.
 *
 * @since 1.0.0
 */
public class EnvironmentSecretSource implements SecretSource {

    private final Environment environment;

    /**
     * Constructs the source.
     *
     * @param environment the Spring environment; must not be {@code null}
     */
    public EnvironmentSecretSource(Environment environment) {
        this.environment = environment;
    }

    /**
     * {@inheritDoc}
     *
     * @param name the property name; must not be {@code null}
     * @return the property value, or {@link Optional#empty()} if unset
     */
    @Override
    public Optional<String> get(String name) {
        return Optional.ofNullable(environment.getProperty(name));
    }
}
