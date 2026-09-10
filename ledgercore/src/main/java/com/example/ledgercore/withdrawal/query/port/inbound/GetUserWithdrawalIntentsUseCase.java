package com.example.ledgercore.withdrawal.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.withdrawal.query.dto.GetUserWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;

public interface GetUserWithdrawalIntentsUseCase {

    PageResponse<WithdrawalIntentResponse> execute(
            GetUserWithdrawalIntentsQuery query
    );
}