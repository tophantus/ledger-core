package com.example.ledgercore.notification.mail.command.port.outbound;

import com.example.ledgercore.notification.mail.command.port.outbound.dto.TransferIntentNotificationInfo;

import java.util.UUID;

public interface TransferIntentQueryPort {

    TransferIntentNotificationInfo getTransferIntent(
            UUID transferIntentId
    );
}