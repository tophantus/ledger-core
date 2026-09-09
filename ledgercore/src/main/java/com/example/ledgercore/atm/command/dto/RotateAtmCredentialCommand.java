package com.example.ledgercore.atm.command.dto;

import java.util.UUID;

public record RotateAtmCredentialCommand(
        UUID terminalId
) {
}