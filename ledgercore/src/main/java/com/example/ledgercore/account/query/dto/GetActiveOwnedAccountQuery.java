package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record GetActiveOwnedAccountQuery(
        UUID customerId,
        UUID accountId
) {
}