package com.example.ledgercore.card.adapter.inbound.rest.dto;

import java.util.UUID;

public record CreateDebitCardRequest(

        UUID accountId,

        String pin
) {
}