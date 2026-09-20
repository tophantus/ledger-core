package com.example.ledgercore.card.adapter.inbound.rest.dto;

import java.util.UUID;

public record CreateCreditCardRequest(
        UUID creditFacilityId,
        String pin
) {
}