package com.example.ledgercore.card.command.dto;

import java.util.UUID;

public record CreateDebitCardCommand(
        UUID customerId,
        UUID accountId,
        String pin
) {
}