package com.example.ledgercore.credit.query.dto;

import java.util.UUID;

public record GetLatestCreditOfferQuery(
        UUID customerId
) {
}