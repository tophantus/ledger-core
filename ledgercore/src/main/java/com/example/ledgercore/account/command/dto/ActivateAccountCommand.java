package com.example.ledgercore.account.command.dto;

import java.util.UUID;

public record ActivateAccountCommand(
        UUID userId,
        UUID accountId
) {
}