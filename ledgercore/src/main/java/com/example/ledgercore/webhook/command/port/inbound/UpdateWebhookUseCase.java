package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.UpdateWebhookCommand;
import com.example.ledgercore.webhook.command.dto.UpdateWebhookResult;

public interface UpdateWebhookUseCase {

    UpdateWebhookResult execute(
            UpdateWebhookCommand command
    );
}