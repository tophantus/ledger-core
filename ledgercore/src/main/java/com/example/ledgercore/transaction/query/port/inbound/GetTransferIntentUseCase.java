package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.TransferIntentNotificationInfo;

import java.util.UUID;

public interface GetTransferIntentUseCase {

    TransferIntentNotificationInfo execute(
            UUID transferIntentId
    );
}