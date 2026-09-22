package com.example.ledgercore.provider.command.dto;

import com.example.ledgercore.provider.enums.ProviderType;

public record RegisterPaymentProviderCommand(
        String code,
        String name,
        ProviderType type
) {
}