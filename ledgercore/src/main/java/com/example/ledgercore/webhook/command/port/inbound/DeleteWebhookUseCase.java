package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.DeleteWebhookCommand;

public interface DeleteWebhookUseCase {

    void execute(DeleteWebhookCommand command);
}