package com.example.ledgercore.credit.command.dto;

import java.util.UUID;

public record RejectCreditOfferCommand(
        UUID offerId,
        UUID customerId
) {
}