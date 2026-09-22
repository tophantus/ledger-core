package com.example.ledgercore.card.command.dto;

import java.util.UUID;

public record VerifyCardCredentialsResult(
        UUID cardId
) {
}