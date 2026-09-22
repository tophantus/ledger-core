package com.example.ledgercore.cardtoken.command.dto;

public record ResumeCardTokenCommand(
        String clientId,
        String credential,
        String token
) {
}