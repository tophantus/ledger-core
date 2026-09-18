package com.example.ledgercore.card.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AesGcmCardEncryptionService
        implements CardEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;

    private final CardEncryptionProperties properties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encrypt(String plaintext) {
        String activeVersion = getActiveEncryptionVersion();

        SecretKeySpec secretKey = getSecretKey(activeVersion);

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    )
            );

            byte[] encrypted = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8)
            );

            return encode(iv, encrypted);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to encrypt card secret",
                    e
            );
        }
    }

    @Override
    public String decrypt(
            String ciphertext,
            String encryptionVersion
    ) {
        if (encryptionVersion == null
                || encryptionVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Encryption version must not be blank"
            );
        }

        SecretKeySpec secretKey =
                getSecretKey(encryptionVersion);

        try {
            byte[] payload = decode(ciphertext);

            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException(
                        "Invalid encrypted card secret"
                );
            }

            ByteBuffer buffer =
                    ByteBuffer.wrap(payload);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);

            byte[] encrypted =
                    new byte[buffer.remaining()];

            buffer.get(encrypted);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    )
            );

            byte[] plaintext =
                    cipher.doFinal(encrypted);

            return new String(
                    plaintext,
                    StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to decrypt card secret",
                    e
            );
        }
    }

    @Override
    public String getActiveEncryptionVersion() {
        String activeVersion =
                properties.getActiveVersion();

        if (activeVersion == null
                || activeVersion.isBlank()) {
            throw new IllegalStateException(
                    "Active card encryption version is not configured"
            );
        }

        return activeVersion;
    }

    private SecretKeySpec getSecretKey(
            String encryptionVersion
    ) {
        Map<String, String> keys =
                properties.getKeys();

        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException(
                    "No card encryption keys are configured"
            );
        }

        String base64Key =
                keys.get(encryptionVersion);

        if (base64Key == null
                || base64Key.isBlank()) {
            throw new IllegalStateException(
                    "Card encryption key not found for version: "
                            + encryptionVersion
            );
        }

        byte[] key;

        try {
            key = Base64.getDecoder()
                    .decode(base64Key);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Card encryption key must be valid Base64: "
                            + encryptionVersion,
                    e
            );
        }

        if (key.length != AES_256_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "Card encryption key must be 256 bits: "
                            + encryptionVersion
            );
        }

        return new SecretKeySpec(
                key,
                KEY_ALGORITHM
        );
    }

    private String encode(
            byte[] iv,
            byte[] encrypted
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(
                iv.length + encrypted.length
        );

        buffer.put(iv);
        buffer.put(encrypted);

        return Base64.getEncoder()
                .encodeToString(buffer.array());
    }

    private byte[] decode(String ciphertext) {
        try {
            return Base64.getDecoder()
                    .decode(ciphertext);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid Base64 encrypted card secret",
                    e
            );
        }
    }
}