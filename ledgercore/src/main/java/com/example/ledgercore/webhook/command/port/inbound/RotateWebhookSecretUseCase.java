package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretCommand;
import com.example.ledgercore.webhook.command.dto.RotateWebhookSecretResult;

public interface RotateWebhookSecretUseCase {

    RotateWebhookSecretResult execute(
            RotateWebhookSecretCommand command
    );
}