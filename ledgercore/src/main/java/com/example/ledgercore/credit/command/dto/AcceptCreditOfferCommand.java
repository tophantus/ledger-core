package com.example.ledgercore.credit.command.dto;

import java.util.UUID;

public record AcceptCreditOfferCommand(
        UUID customerId,
        UUID offerId
) {
}