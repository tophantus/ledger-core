package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountTransferInfo;

import java.util.UUID;

public interface GetTransferAccountInfoUseCase {

    AccountTransferInfo execute(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    );
}