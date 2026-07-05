/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WebhookSignerTest {

    private final WebhookSigner signer = new WebhookSigner("HmacSHA256");

    @Test
    void should_produceDeterministicSignature() {
        String first = signer.sign("{\"a\":1}", "topsecret");
        String second = signer.sign("{\"a\":1}", "topsecret");
        assertThat(first).isEqualTo(second).startsWith("sha256=");
    }

    @Test
    void should_differ_when_secretDiffers() {
        assertThat(signer.sign("payload", "secret-a"))
            .isNotEqualTo(signer.sign("payload", "secret-b"));
    }

    @Test
    void should_matchKnownVector() {
        // HMAC-SHA256("hello", key="key") — a stable, independently verifiable vector.
        assertThat(signer.sign("hello", "key"))
            .isEqualTo("sha256=9307b3b915efb5171ff14d8cb55fbcc798c6c0ef1456d66ded1a6aa723a58b7b");
    }
}
