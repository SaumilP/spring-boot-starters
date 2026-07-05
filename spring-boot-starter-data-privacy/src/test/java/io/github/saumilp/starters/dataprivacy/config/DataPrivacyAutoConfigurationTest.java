/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.config;

import io.github.saumilp.starters.dataprivacy.crypto.AesGcmEncryptor;
import io.github.saumilp.starters.dataprivacy.masking.MaskingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DataPrivacyAutoConfiguration} conditional wiring.
 */
class DataPrivacyAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DataPrivacyAutoConfiguration.class));

    @Test
    void should_registerMaskingButNoEncryptor_byDefault() {
        runner.run(context -> assertThat(context)
            .hasSingleBean(MaskingService.class)
            .doesNotHaveBean(AesGcmEncryptor.class));
    }

    @Test
    void should_registerEncryptor_when_keySet() {
        runner.withPropertyValues("spring.data-privacy.encryption.key=super-secret")
            .run(context -> assertThat(context)
                .hasSingleBean(AesGcmEncryptor.class)
                .hasSingleBean(MaskingService.class));
    }

    @Test
    void should_notRegisterMasking_when_maskingDisabled() {
        runner.withPropertyValues("spring.data-privacy.masking.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MaskingService.class));
    }

    @Test
    void should_registerNothing_when_disabled() {
        runner.withPropertyValues("spring.data-privacy.enabled=false",
                "spring.data-privacy.encryption.key=super-secret")
            .run(context -> assertThat(context)
                .doesNotHaveBean(MaskingService.class)
                .doesNotHaveBean(AesGcmEncryptor.class));
    }
}
