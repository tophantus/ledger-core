package com.example.ledgercore.account.query.service.dto;

import java.util.UUID;

public record GetUserAccountQuery(
        UUID accountId
) {
}