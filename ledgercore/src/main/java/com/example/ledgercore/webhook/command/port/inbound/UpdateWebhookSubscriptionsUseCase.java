package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.UpdateWebhookSubscriptionsCommand;

public interface UpdateWebhookSubscriptionsUseCase {

    void execute(
            UpdateWebhookSubscriptionsCommand command
    );
}