package com.example.ledgercore.user.command.dto;

import java.util.UUID;

public record ActivateUserCommand(
        UUID userId
) {
}