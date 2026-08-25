package com.example.ledgercore.webhook.command.port.outbound;

public interface WebhookHttpClientPort {

    void send(
            String url,
            String secret,
            String payload
    );
}