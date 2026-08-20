package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.TransferAccountInfo;

import java.util.UUID;

public interface GetTransferAccountInfoUseCase {

    TransferAccountInfo execute(
            UUID userId,
            UUID sourceAccountId,
            String destinationAccountNo
    );
}