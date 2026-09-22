package com.example.ledgercore.provider.command.service;

public interface PaymentProviderCredentialHashService {

    String hash(String credential);

    boolean matches(String credential, String credentialHash);
}