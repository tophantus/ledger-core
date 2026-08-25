package com.example.ledgercore.webhook.command.port.inbound;

import java.util.UUID;

public interface ProcessWebhookDeliveryUseCase {

    void execute(UUID deliveryId);
}