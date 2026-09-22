package com.example.ledgercore.provider.command.dto;

public record AuthenticatePaymentProviderCommand(
        String clientId,
        String credential
) {
}