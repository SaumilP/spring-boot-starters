/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA {@link AttributeConverter} that transparently encrypts a {@code String} entity attribute at
 * rest using {@link AesGcmEncryptor}.
 *
 * <p>Apply it to sensitive fields:
 * <pre>{@code
 * @Convert(converter = EncryptedStringConverter.class)
 * @Column(name = "national_id_number")
 * private String nationalIdNumber;
 * }</pre>
 *
 * <p>The encryptor is resolved from {@link FieldEncryptorHolder}, which the data-privacy
 * auto-configuration populates when {@code spring.data-privacy.encryption.key} is set.
 *
 * @since 1.0.0
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    /** Creates a new converter. */
    public EncryptedStringConverter() {
    }

    /**
     * {@inheritDoc}
     *
     * @param attribute the plaintext attribute value; may be {@code null}
     * @return the encrypted column value, or {@code null} if {@code attribute} is {@code null}
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : FieldEncryptorHolder.getEncryptor().encrypt(attribute);
    }

    /**
     * {@inheritDoc}
     *
     * @param dbData the encrypted column value; may be {@code null}
     * @return the decrypted plaintext, or {@code null} if {@code dbData} is {@code null}
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FieldEncryptorHolder.getEncryptor().decrypt(dbData);
    }
}
