package com.example.ledgercore.card.infrastructure.security;

public interface CardSecretHashService {

    String hash(String secret);

    boolean matches(String secret, String hash);
}