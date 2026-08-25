package com.example.ledgercore.webhook.command.port.inbound;

import com.example.ledgercore.webhook.command.dto.RegisterWebhookCommand;

import java.util.UUID;

public interface RegisterWebhookUseCase {

    UUID execute(RegisterWebhookCommand command);
}