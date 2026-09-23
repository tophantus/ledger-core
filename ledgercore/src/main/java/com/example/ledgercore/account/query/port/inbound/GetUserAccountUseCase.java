package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetUserAccountQuery;

public interface GetUserAccountUseCase {

    AccountResponse execute(GetUserAccountQuery query);
}