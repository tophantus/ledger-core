package com.example.ledgercore.withdrawal.command.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateWithdrawalLookupCodeCommand(
        UUID withdrawalIntentId,
        Instant expiresAt
) {}