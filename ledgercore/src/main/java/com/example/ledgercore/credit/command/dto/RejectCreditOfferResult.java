package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.credit.enums.CreditOfferStatus;

import java.time.Instant;
import java.util.UUID;

public record RejectCreditOfferResult(
        UUID offerId,
        UUID customerId,
        CreditOfferStatus status,
        Instant rejectedAt
) {
}