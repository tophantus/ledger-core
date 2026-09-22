package com.example.ledgercore.cardtoken.command.dto;

public record RevokeCardTokenCommand(
        String clientId,
        String credential,
        String token
) {
}