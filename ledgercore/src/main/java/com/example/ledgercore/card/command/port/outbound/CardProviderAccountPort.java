package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public interface CardProviderAccountPort {

    UUID getProviderAccountId(
            UUID providerId,
            Currency currency
    );
}
