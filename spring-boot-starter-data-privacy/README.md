# spring-boot-starter-data-privacy

PII protection for Spring Boot: **value/log masking** and **JPA field-level AES-GCM encryption**.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-data-privacy:1.0.0'
}
```

---

## What you get

| Bean / type | Condition | Purpose |
|---|---|---|
| `MaskingService` | masking enabled (default) | Masks emails, card numbers, or whole values for display/logging |
| `AesGcmEncryptor` | `spring.data-privacy.encryption.key` set | AES-256-GCM encryptor used by the JPA converter |
| `EncryptedStringConverter` | provided | JPA `AttributeConverter` that encrypts a `String` column at rest |

Beans are `@ConditionalOnMissingBean`.

---

## Field-level encryption

Set a key and annotate the sensitive entity fields:

```yaml
spring:
  data-privacy:
    encryption:
      key: ${FIELD_ENCRYPTION_KEY}   # a strong, random secret
```

```java
@Convert(converter = EncryptedStringConverter.class)
@Column(name = "national_id_number")
private String nationalIdNumber;
```

Values are encrypted with a fresh random IV per write (AES-256-GCM), so the same plaintext yields
different ciphertext each time while remaining decryptable. The key is derived from the configured
secret via SHA-256.

> The converter is instantiated by the JPA provider (not Spring), so the encryptor is resolved from
> a static `FieldEncryptorHolder` populated at startup. If encryption is used without a configured
> key, the converter fails fast with a clear message.

---

## Masking

```java
maskingService.mask("john.doe@example.com", MaskStrategy.EMAIL);   // j***@example.com
maskingService.mask("4111111111111111", MaskStrategy.CREDIT_CARD); // ************1111
maskingService.mask("secret", MaskStrategy.FULL);                  // ******
```

`null`/empty inputs are returned unchanged.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.data-privacy.enabled` | `true` | Master switch |
| `spring.data-privacy.encryption.key` | *(unset)* | Secret material for the AES key; encryptor registered only when set |
| `spring.data-privacy.masking.enabled` | `true` | Register the masking service |

---

## Notes

- Uses only the JDK crypto provider (AES-GCM) — no third-party crypto dependency.
- Annotation-driven masking (a `@Masked` Jackson module) and blind-index search columns are planned
  enhancements; v1 ships the `MaskingService` and the encrypting JPA converter.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
