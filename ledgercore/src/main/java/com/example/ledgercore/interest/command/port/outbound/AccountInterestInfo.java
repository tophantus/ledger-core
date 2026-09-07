package com.example.ledgercore.interest.command.port.outbound;

import java.util.UUID;

public record AccountInterestInfo(
        UUID accountId,
        String productCode,
        String currency,
        boolean active
) {
}