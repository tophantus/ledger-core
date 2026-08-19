package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountByAccountNoQuery;

public interface GetAccountByAccountNoUseCase {

    AccountResponse execute(GetAccountByAccountNoQuery query);
}