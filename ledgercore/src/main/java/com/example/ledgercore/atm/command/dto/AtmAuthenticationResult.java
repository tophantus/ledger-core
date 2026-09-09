package com.example.ledgercore.atm.command.dto;

import java.util.UUID;

public record AtmAuthenticationResult(
        UUID atmTerminalId
) {
}