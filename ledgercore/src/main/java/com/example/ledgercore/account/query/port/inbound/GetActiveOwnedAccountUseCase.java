package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountQuery;
import com.example.ledgercore.account.query.dto.GetActiveOwnedAccountResult;

public interface GetActiveOwnedAccountUseCase {

    GetActiveOwnedAccountResult execute(
            GetActiveOwnedAccountQuery query
    );
}