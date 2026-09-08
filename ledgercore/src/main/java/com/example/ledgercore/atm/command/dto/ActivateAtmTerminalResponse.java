package com.example.ledgercore.atm.command.dto;

import com.example.ledgercore.atm.enums.AtmTerminalStatus;

import java.util.UUID;

public record ActivateAtmTerminalResponse(
        UUID terminalId,
        String terminalCode,
        AtmTerminalStatus status
) {
}