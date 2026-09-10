package com.example.ledgercore.withdrawal.command.dto;

import java.util.UUID;

public record CancelWithdrawalIntentCommand(
        UUID userId,
        UUID intentId
) {
}