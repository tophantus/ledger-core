package com.example.ledgercore.withdrawal.command.dto;

import java.util.UUID;

public record ConfirmWithdrawalRequestCommand(
        UUID userId,
        UUID requestId,
        String otp
) {
}