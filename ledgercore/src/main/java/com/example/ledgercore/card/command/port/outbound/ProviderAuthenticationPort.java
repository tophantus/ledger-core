package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.provider.enums.ProviderType;

import java.util.UUID;

public interface ProviderAuthenticationPort {

    ProviderAuthenticationResult authenticate(
            String clientId,
            String credential
    );

    record ProviderAuthenticationResult(
            UUID providerId,
            String code,
            String name,
            ProviderType type
    ) {
    }
}
