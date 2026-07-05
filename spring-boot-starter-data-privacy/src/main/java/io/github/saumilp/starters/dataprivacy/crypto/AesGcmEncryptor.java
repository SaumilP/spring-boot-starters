/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Symmetric field encryptor using AES-256 in GCM mode.
 *
 * <p>The 256-bit key is derived from the configured secret via SHA-256. Each {@link #encrypt}
 * call uses a fresh random 96-bit IV, which is prepended to the ciphertext and Base64-encoded, so
 * the same plaintext encrypts to a different value every time while remaining decryptable.
 *
 * @since 1.0.0
 */
public class AesGcmEncryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates the encryptor, deriving a 256-bit AES key from the given secret.
     *
     * @param secret the secret material; must not be {@code null} or blank
     */
    public AesGcmEncryptor(String secret) {
        this.key = deriveKey(secret);
    }

    private static SecretKey deriveKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (GeneralSecurityException ex) {
            throw new EncryptionException("Failed to derive AES key", ex);
        }
    }

    /**
     * Encrypts the given plaintext.
     *
     * @param plaintext the value to encrypt; must not be {@code null}
     * @return the Base64-encoded {@code IV || ciphertext}; never {@code null}
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            throw new EncryptionException("Failed to encrypt value", ex);
        }
    }

    /**
     * Decrypts a value previously produced by {@link #encrypt}.
     *
     * @param ciphertext the Base64-encoded {@code IV || ciphertext}; must not be {@code null}
     * @return the decrypted plaintext; never {@code null}
     * @throws EncryptionException if decryption fails (e.g. wrong key or tampered data)
     */
    public String decrypt(String ciphertext) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new EncryptionException("Failed to decrypt value", ex);
        }
    }
}
