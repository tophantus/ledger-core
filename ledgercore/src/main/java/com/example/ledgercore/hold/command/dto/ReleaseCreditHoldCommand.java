package com.example.ledgercore.hold.command.dto;

import java.util.UUID;

public record ReleaseCreditHoldCommand(
        UUID holdId
) {
}