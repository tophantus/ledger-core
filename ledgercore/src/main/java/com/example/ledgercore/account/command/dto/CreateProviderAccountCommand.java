package com.example.ledgercore.account.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record CreateProviderAccountCommand(
        UUID providerId,
        Currency currency
) {
}