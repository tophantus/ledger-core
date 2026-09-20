package com.example.ledgercore.credit.command.service.dto;

import java.util.UUID;

public record EvaluateCreditOfferCommand(
        UUID customerId
) {
}