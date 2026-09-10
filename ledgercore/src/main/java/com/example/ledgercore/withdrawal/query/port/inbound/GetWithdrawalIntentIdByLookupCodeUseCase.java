package com.example.ledgercore.withdrawal.query.port.inbound;

import com.example.ledgercore.withdrawal.query.dto.GetWithdrawalIntentIdByLookupCodeQuery;

import java.util.UUID;

public interface GetWithdrawalIntentIdByLookupCodeUseCase {

    UUID execute(
            GetWithdrawalIntentIdByLookupCodeQuery query
    );
}