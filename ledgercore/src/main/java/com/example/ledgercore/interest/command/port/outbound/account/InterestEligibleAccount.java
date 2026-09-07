package com.example.ledgercore.interest.command.port.outbound.account;

import java.util.UUID;

public record InterestEligibleAccount(
        UUID accountId,
        UUID productId,
        String currency
) {
}