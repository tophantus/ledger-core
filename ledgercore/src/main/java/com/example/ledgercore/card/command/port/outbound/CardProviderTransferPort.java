package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardProviderTransferPort {

    UUID transferFromDebitCard(
            UUID sourceAccountId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    );

    UUID transferFromCreditCard(
            UUID creditFacilityId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    );
}
