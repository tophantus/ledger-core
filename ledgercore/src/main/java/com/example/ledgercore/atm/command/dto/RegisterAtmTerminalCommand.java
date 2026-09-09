package com.example.ledgercore.atm.command.dto;

public record RegisterAtmTerminalCommand(
        String terminalCode,
        String location
) {
}