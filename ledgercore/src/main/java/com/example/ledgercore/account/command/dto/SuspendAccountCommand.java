package com.example.ledgercore.account.command.dto;

import java.util.UUID;

public record SuspendAccountCommand(
        UUID accountId
) {
}