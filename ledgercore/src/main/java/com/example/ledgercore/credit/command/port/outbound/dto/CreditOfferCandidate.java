package com.example.ledgercore.credit.command.port.outbound.dto;

import java.util.UUID;

public record CreditOfferCandidate(
        UUID customerId
) {
}