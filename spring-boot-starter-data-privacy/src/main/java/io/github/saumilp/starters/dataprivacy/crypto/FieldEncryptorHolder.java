/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

/**
 * Static holder that exposes the configured {@link AesGcmEncryptor} to
 * {@link EncryptedStringConverter} instances.
 *
 * <p>JPA {@code AttributeConverter}s are instantiated by the persistence provider rather than the
 * Spring container, so they cannot rely on dependency injection. The data-privacy
 * auto-configuration publishes the encryptor here at startup, and the converter reads it on demand.
 *
 * @since 1.0.0
 */
public final class FieldEncryptorHolder {

    private static volatile AesGcmEncryptor encryptor;

    private FieldEncryptorHolder() {
    }

    /**
     * Publishes the encryptor for use by converters.
     *
     * @param value the encryptor; must not be {@code null}
     */
    public static void setEncryptor(AesGcmEncryptor value) {
        encryptor = value;
    }

    /**
     * Returns the published encryptor.
     *
     * @return the encryptor; never {@code null}
     * @throws IllegalStateException if no encryptor has been configured (the encryption key is unset)
     */
    public static AesGcmEncryptor getEncryptor() {
        AesGcmEncryptor current = encryptor;
        if (current == null) {
            throw new IllegalStateException(
                "Field encryptor is not initialised; set 'spring.data-privacy.encryption.key'");
        }
        return current;
    }
}
