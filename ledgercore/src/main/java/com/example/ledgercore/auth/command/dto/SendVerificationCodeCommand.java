package com.example.ledgercore.auth.command.dto;

public record SendVerificationCodeCommand(
        String email
) {
}