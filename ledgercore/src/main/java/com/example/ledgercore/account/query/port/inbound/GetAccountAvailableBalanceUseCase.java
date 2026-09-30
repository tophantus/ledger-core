package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceQuery;
import com.example.ledgercore.account.query.dto.GetAccountAvailableBalanceResult;

public interface GetAccountAvailableBalanceUseCase {

    GetAccountAvailableBalanceResult execute(
            GetAccountAvailableBalanceQuery query
    );
}