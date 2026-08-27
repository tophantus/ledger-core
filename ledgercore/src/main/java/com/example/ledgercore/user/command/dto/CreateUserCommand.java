package com.example.ledgercore.user.command.dto;

public record CreateUserCommand(
        String fullName,
        String email,
        String passwordHash
) {
}