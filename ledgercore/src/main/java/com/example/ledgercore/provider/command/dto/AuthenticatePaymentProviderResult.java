package com.example.ledgercore.provider.command.dto;

import com.example.ledgercore.provider.enums.ProviderStatus;
import com.example.ledgercore.provider.enums.ProviderType;

import java.util.UUID;

public record AuthenticatePaymentProviderResult(
        UUID providerId,
        String code,
        String name,
        ProviderType type,
        ProviderStatus status
) {
}