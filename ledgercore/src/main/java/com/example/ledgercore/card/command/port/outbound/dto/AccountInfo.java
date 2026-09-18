package com.example.ledgercore.card.command.port.outbound.dto;

import java.util.UUID;

public record AccountInfo(
        UUID id,
        UUID userId,
        UUID productId
) {
}