package com.example.ledgercore.account.command.dto;

import java.util.UUID;

public record CloseAccountCommand(
        UUID userId,
        UUID accountId
) {
}