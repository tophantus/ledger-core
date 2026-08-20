package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetActiveUserAccountsQuery;

import java.util.List;

public interface GetUserActiveAccountsUseCase {

    List<AccountSummaryResponse> execute(GetActiveUserAccountsQuery query);
}