package com.example.ledgercore.account.query.dto;

public record GetProviderAccountsQuery(
        String clientId,
        String credential
) {
}