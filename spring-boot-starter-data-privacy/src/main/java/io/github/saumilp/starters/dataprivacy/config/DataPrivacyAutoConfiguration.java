/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.config;

import io.github.saumilp.starters.dataprivacy.crypto.AesGcmEncryptor;
import io.github.saumilp.starters.dataprivacy.crypto.FieldEncryptorHolder;
import io.github.saumilp.starters.dataprivacy.masking.MaskingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the data-privacy starter.
 *
 * <p>Registers a {@link MaskingService} (unless disabled) and, when
 * {@code spring.data-privacy.encryption.key} is set, an {@link AesGcmEncryptor} that is also
 * published to {@link FieldEncryptorHolder} for use by {@code EncryptedStringConverter}.
 *
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(DataPrivacyProperties.class)
@ConditionalOnProperty(prefix = "spring.data-privacy", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class DataPrivacyAutoConfiguration {

    /** Creates the data-privacy auto-configuration. */
    public DataPrivacyAutoConfiguration() {
    }

    /**
     * Registers the value/log masking service.
     *
     * @return a {@link MaskingService}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.data-privacy.masking", name = "enabled",
        havingValue = "true", matchIfMissing = true)
    public MaskingService maskingService() {
        return new MaskingService();
    }

    /**
     * Registers the field encryptor and publishes it to {@link FieldEncryptorHolder}. Only active
     * when {@code spring.data-privacy.encryption.key} is set.
     *
     * @param properties the data-privacy configuration; must not be {@code null}
     * @return an {@link AesGcmEncryptor}; never {@code null}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.data-privacy.encryption", name = "key")
    public AesGcmEncryptor fieldEncryptor(DataPrivacyProperties properties) {
        AesGcmEncryptor encryptor = new AesGcmEncryptor(properties.getEncryption().getKey());
        FieldEncryptorHolder.setEncryptor(encryptor);
        return encryptor;
    }
}
