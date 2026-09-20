package com.example.ledgercore.card.infrastructure.security;

public interface CardEncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext, String encryptionVersion);

    String getActiveEncryptionVersion();
}