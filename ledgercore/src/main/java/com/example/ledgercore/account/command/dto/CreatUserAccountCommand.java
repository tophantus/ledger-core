package com.example.ledgercore.account.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record CreatUserAccountCommand(
        UUID userId,
        UUID productId,
        Currency currency
) {
}