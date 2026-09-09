package com.example.ledgercore.atm.command.dto;

import com.example.ledgercore.atm.enums.AtmTerminalStatus;

import java.time.Instant;
import java.util.UUID;

public record RegisterAtmTerminalResponse(
        UUID id,
        String terminalCode,
        String credential,
        AtmTerminalStatus status,
        String location,
        Instant createdAt
) {
}