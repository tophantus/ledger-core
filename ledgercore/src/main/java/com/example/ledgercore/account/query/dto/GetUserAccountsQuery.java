package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record GetUserAccountsQuery(
        UUID userId
) {
}