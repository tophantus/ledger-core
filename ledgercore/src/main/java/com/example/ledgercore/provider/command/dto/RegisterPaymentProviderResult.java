package com.example.ledgercore.provider.command.dto;

import com.example.ledgercore.provider.enums.ProviderStatus;
import com.example.ledgercore.provider.enums.ProviderType;

import java.util.UUID;

public record RegisterPaymentProviderResult(
        UUID providerId,
        String code,
        String name,
        ProviderType type,
        ProviderStatus status,
        String clientId,
        String credential
) {
}