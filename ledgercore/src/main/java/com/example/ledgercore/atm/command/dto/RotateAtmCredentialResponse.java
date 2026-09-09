package com.example.ledgercore.atm.command.dto;

import java.util.UUID;

public record RotateAtmCredentialResponse(
        UUID terminalId,
        String terminalCode,
        String credential
) {
}