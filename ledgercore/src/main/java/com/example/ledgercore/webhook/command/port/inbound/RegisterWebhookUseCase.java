package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;
import com.example.ledgercore.webhook.command.dto.RegisterWebhookResult;

public interface RegisterWebhookUseCase {

    RegisterWebhookResult execute(RegisterWebhookCommand command);
}