package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountDepositInfo;

import java.util.UUID;

public interface GetDepositAccountInfoUseCase {

    AccountDepositInfo execute(
            UUID accountId
    );
}