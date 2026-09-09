package com.example.ledgercore.atm.command.dto;

public record AuthenticateAtmCommand(
        String terminalCode,
        String credential
) {
}