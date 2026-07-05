/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.webhooks.delivery;

import io.github.saumilp.starters.common.exception.StarterConfigurationException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Computes an HMAC signature over a webhook payload so subscribers can verify authenticity.
 *
 * <p>The signature is returned as a lowercase hex string prefixed with the short algorithm name
 * (for example {@code sha256=ab12...}), matching the convention used by GitHub, Stripe, and other
 * webhook providers.
 *
 * @author SaumilP
 * @since 1.0.0
 */
public class WebhookSigner {

    private final String algorithm;
    private final String prefix;

    /**
     * Creates a signer for the given MAC algorithm.
     *
     * @param algorithm a JCA {@code Mac} algorithm name (e.g. {@code HmacSHA256}); must not be {@code null}
     */
    public WebhookSigner(String algorithm) {
        this.algorithm = algorithm;
        this.prefix = shortName(algorithm);
    }

    private static String shortName(String algorithm) {
        String lower = algorithm.toLowerCase();
        if (lower.contains("sha256")) {
            return "sha256";
        }
        if (lower.contains("sha512")) {
            return "sha512";
        }
        if (lower.contains("sha1")) {
            return "sha1";
        }
        return lower;
    }

    /**
     * Signs the payload with the endpoint secret.
     *
     * @param payload the exact request body bytes will be derived from this string (UTF-8); must not be {@code null}
     * @param secret  the shared signing secret; must not be {@code null}
     * @return the {@code prefix=hex} signature; never {@code null}
     */
    public String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return prefix + "=" + toHex(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new StarterConfigurationException(
                "Unable to compute webhook signature with algorithm " + algorithm, ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
