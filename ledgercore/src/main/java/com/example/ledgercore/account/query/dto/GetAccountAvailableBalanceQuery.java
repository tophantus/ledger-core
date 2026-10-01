package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record GetAccountAvailableBalanceQuery(
        UUID accountId
) {
}