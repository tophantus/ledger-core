package com.example.ledgercore.cardtoken.command.port.outbound;

import java.util.UUID;

public interface CardVerificationPort {

    CardVerificationResult verify(
            String pan,
            Short expiryMonth,
            Short expiryYear,
            String cvv
    );

    record CardVerificationResult(
            UUID cardId
    ) {
    }
}