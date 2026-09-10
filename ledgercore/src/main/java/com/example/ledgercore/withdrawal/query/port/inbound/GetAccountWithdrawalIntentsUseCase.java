package com.example.ledgercore.withdrawal.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.withdrawal.query.dto.GetAccountWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;

public interface GetAccountWithdrawalIntentsUseCase {

    PageResponse<WithdrawalIntentResponse> execute(
            GetAccountWithdrawalIntentsQuery query
    );
}