package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.DepositAccountInfo;

import java.util.UUID;

public interface GetDepositAccountInfoUseCase {

    DepositAccountInfo execute(
            UUID accountId
    );
}