package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetProviderAccountsQuery;

import java.util.List;

public interface GetProviderAccountsUseCase {

    List<AccountSummaryResponse> execute(
            GetProviderAccountsQuery query
    );
}