package com.example.ledgercore.atm.query.dto;

import com.example.ledgercore.atm.enums.AtmTerminalStatus;

import java.time.Instant;
import java.util.UUID;

public record AtmTerminalResponse(
        UUID id,
        String terminalCode,
        AtmTerminalStatus status,
        String location,
        Instant createdAt,
        Instant updatedAt
) {
}