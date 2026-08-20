package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record GetAccountByAccountNoQuery(
        UUID userId,
        String accountNo
) {
}