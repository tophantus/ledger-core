package com.example.ledgercore.common.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmEncryptionService
        implements EncryptionService {

    private static final String VERSION = "v1";
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public AesGcmEncryptionService(
            @Value("${security.encryption.key}") String key
    ) {
        byte[] keyBytes =
                Base64.getDecoder().decode(key);

        if (keyBytes.length != KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "Encryption key must be 256 bits"
            );
        }

        this.secretKey =
                new SecretKeySpec(keyBytes, "AES");

        this.secureRandom = new SecureRandom();
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    )
            );

            byte[] ciphertext =
                    cipher.doFinal(
                            plaintext.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            byte[] payload =
                    ByteBuffer
                            .allocate(
                                    iv.length +
                                            ciphertext.length
                            )
                            .put(iv)
                            .put(ciphertext)
                            .array();

            return VERSION + ":" +
                    Base64.getEncoder()
                            .encodeToString(payload);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to encrypt data",
                    e
            );
        }
    }

    @Override
    public String decrypt(String ciphertext) {

        String[] parts = ciphertext.split(":", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid encrypted data format"
            );
        }

        return switch (parts[0]) {
            case "v1" -> decryptV1(parts[1]);
            default -> throw new IllegalArgumentException(
                    "Unsupported encryption version: "
                            + parts[0]
            );
        };
    }

    private String decryptV1(String encodedPayload) {
        try {
            byte[] payload =
                    Base64.getDecoder()
                            .decode(encodedPayload);

            if (payload.length <= IV_LENGTH) {
                throw new IllegalArgumentException(
                        "Invalid encrypted data"
                );
            }

            ByteBuffer buffer =
                    ByteBuffer.wrap(payload);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encryptedData =
                    new byte[buffer.remaining()];

            buffer.get(encryptedData);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    )
            );

            byte[] plaintext =
                    cipher.doFinal(encryptedData);

            return new String(
                    plaintext,
                    StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException |
                 IllegalArgumentException e) {

            throw new IllegalStateException(
                    "Failed to decrypt data",
                    e
            );
        }
    }
}