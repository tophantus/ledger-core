package com.example.ledgercore.card.command.port.outbound;

import java.util.UUID;

public interface CardTokenPort {

    CardTokenInfo resolve(
            UUID providerId,
            String token
    );

    record CardTokenInfo(
            UUID cardId
    ) {
    }
}
