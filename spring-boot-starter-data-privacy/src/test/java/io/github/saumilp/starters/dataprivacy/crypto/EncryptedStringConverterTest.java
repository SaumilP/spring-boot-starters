/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EncryptedStringConverter}.
 */
class EncryptedStringConverterTest {

    private final EncryptedStringConverter converter = new EncryptedStringConverter();

    @BeforeEach
    void setUp() {
        FieldEncryptorHolder.setEncryptor(new AesGcmEncryptor("converter-test-secret"));
    }

    @Test
    void should_encryptAndDecryptRoundTrip() {
        String encrypted = converter.convertToDatabaseColumn("sensitive");

        assertThat(encrypted).isNotEqualTo("sensitive");
        assertThat(converter.convertToEntityAttribute(encrypted)).isEqualTo("sensitive");
    }

    @Test
    void should_passThroughNulls() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
