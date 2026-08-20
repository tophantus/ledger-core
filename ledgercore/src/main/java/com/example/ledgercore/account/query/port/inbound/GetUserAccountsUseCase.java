package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetUserAccountsQuery;

import java.util.List;

public interface GetUserAccountsUseCase {

    List<AccountSummaryResponse> execute(GetUserAccountsQuery query);
}