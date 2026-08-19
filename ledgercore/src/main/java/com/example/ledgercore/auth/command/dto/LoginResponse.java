package com.example.ledgercore.auth.command.dto;

import com.example.ledgercore.auth.command.enums.LoginStatus;

import java.util.UUID;

public record LoginResponse(
        LoginStatus status,
        TokenResponse token,
        UUID userId
) {
}