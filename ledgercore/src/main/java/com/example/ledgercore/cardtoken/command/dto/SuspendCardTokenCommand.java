package com.example.ledgercore.cardtoken.command.dto;

public record SuspendCardTokenCommand(
        String clientId,
        String credential,
        String token
) {
}