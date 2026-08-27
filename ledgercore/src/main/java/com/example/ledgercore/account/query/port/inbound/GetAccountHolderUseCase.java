package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountHolderResponse;
import com.example.ledgercore.account.query.dto.GetAccountHolderQuery;

public interface GetAccountHolderUseCase {

    AccountHolderResponse execute(
            GetAccountHolderQuery query
    );
}