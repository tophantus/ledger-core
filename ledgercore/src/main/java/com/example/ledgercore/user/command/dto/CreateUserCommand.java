package com.example.ledgercore.user.command.dto;

public record CreateUserCommand(
        String username,
        String email,
        String passwordHash
) {
}