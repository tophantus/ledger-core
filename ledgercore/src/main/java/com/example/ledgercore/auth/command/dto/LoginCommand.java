package com.example.ledgercore.auth.command.dto;

public record LoginCommand(
        String email,
        String password
) {
}