/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AesGcmEncryptor}.
 */
class AesGcmEncryptorTest {

    private final AesGcmEncryptor encryptor = new AesGcmEncryptor("a-strong-test-secret");

    @Test
    void should_roundTrip() {
        String plaintext = "123-45-6789";
        String encrypted = encryptor.encrypt(plaintext);

        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encryptor.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void should_produceDifferentCiphertext_forSamePlaintext() {
        String a = encryptor.encrypt("same");
        String b = encryptor.encrypt("same");

        assertThat(a).isNotEqualTo(b);
        assertThat(encryptor.decrypt(a)).isEqualTo("same");
        assertThat(encryptor.decrypt(b)).isEqualTo("same");
    }

    @Test
    void should_failToDecrypt_withWrongKey() {
        String encrypted = encryptor.encrypt("secret");
        AesGcmEncryptor other = new AesGcmEncryptor("a-different-secret");

        assertThatThrownBy(() -> other.decrypt(encrypted)).isInstanceOf(EncryptionException.class);
    }
}
