package com.example.ledgercore.cardtoken.command.dto;

import java.util.UUID;

public record ProvisionCardTokenResult(
        UUID tokenId,
        UUID cardId,
        String token,
        UUID providerId,
        String providerCustomerReference
) {
}