package com.example.ledgercore.account.command.dto;

import java.util.UUID;

public record CreateAccountCommand(
        UUID userId,
        String currency
) {
}