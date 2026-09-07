package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AdminAccountFilter;
import com.example.ledgercore.account.query.dto.AdminAccountResponse;
import com.example.ledgercore.common.dto.PageResponse;

public interface GetAdminAccountsUseCase {

    PageResponse<AdminAccountResponse> execute(
            AdminAccountFilter filter
    );
}