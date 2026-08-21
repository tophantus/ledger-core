package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;

import java.util.UUID;

public interface GetWithdrawAccountInfoUseCase {

    AccountWithdrawInfo execute(
            UUID userId,
            UUID accountId
    );
}