package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.transaction.event.TransferCompletedEvent;

public interface HandleTransferCompletedWebhookUseCase {

    void execute(
            TransferCompletedEvent event
    );
}