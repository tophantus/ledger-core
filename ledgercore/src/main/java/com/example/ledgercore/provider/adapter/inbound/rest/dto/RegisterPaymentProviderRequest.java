package com.example.ledgercore.provider.adapter.inbound.rest.dto;

import com.example.ledgercore.provider.enums.ProviderType;

public record RegisterPaymentProviderRequest(
        String code,
        String name,
        ProviderType type
) {
}
