package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetProviderAccountByCredentialQuery;

public interface GetProviderAccountByCredentialUseCase {

    AccountResponse execute(
            GetProviderAccountByCredentialQuery query
    );
}