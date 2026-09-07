package com.example.ledgercore.account.port.outbound;

import java.util.UUID;

public record ProductAccountInfo(
        UUID productId,
        String code
) {
}