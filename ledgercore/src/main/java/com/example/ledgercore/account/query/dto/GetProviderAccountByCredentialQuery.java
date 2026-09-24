package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record GetProviderAccountByCredentialQuery(
        String clientId,
        String credential,
        UUID accountId
) {
}