package com.example.ledgercore.card.command.dto;

public record VerifyCardCredentialsCommand(
        String pan,
        Short expiryMonth,
        Short expiryYear,
        String cvv
) {
}