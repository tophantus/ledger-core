package com.example.ledgercore.card.command.dto;

import java.util.UUID;

public record CreateCreditCardCommand(
        UUID customerId,
        UUID creditFacilityId,
        String pin
) {
}