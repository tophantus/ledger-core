package com.example.ledgercore.provider.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public interface ProviderAccountPort {

    void createProviderAccount(
            UUID providerId,
            Currency currency
    );
}