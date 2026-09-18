package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.util.UUID;

public record GetActiveOwnedAccountResult(
        UUID id,
        UUID userId,
        UUID productId,
        AccountStatus status
) {
}