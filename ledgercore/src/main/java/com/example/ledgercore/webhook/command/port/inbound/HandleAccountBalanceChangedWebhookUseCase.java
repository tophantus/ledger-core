package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;

public interface HandleAccountBalanceChangedWebhookUseCase {

    void execute(
            AccountBalanceChangedEvent event
    );
}