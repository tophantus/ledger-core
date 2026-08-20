package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountQuery;

public interface GetAccountUseCase {

    AccountResponse execute(GetAccountQuery query);
}