package com.example.ledgercore.common.encryption;

public interface EncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}