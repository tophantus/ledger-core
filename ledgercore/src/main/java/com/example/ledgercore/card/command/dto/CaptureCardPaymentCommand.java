package com.example.ledgercore.card.command.dto;

import java.util.UUID;

public record CaptureCardPaymentCommand(
        String providerClientId,
        String providerCredential,
        UUID authorizationId,
        String reference,
        String description
) {
}
