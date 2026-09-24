package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public interface GetProviderAccountUseCase {

    AccountResponse execute(
            UUID providerId,
            Currency currency
    );
}