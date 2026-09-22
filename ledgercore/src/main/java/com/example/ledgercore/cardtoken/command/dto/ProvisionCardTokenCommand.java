package com.example.ledgercore.cardtoken.command.dto;

import java.util.UUID;

public record ProvisionCardTokenCommand(
        String clientId,
        String credential,
        String pan,
        Short expiryMonth,
        Short expiryYear,
        String cvv,
        String providerCustomerReference
) {
}