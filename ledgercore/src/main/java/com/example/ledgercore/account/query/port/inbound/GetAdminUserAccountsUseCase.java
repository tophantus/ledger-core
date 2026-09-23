package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AdminUserAccountFilter;
import com.example.ledgercore.account.query.dto.AdminUserAccountResponse;
import com.example.ledgercore.common.dto.PageResponse;

public interface GetAdminUserAccountsUseCase {

    PageResponse<AdminUserAccountResponse> execute(
            AdminUserAccountFilter filter
    );
}