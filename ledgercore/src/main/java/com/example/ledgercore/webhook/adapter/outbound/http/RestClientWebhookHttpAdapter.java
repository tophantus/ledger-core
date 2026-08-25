package com.example.ledgercore.webhook.adapter.outbound.http;

import com.example.ledgercore.webhook.command.port.outbound.WebhookHttpClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RestClientWebhookHttpAdapter
        implements WebhookHttpClientPort {

    private final RestClient webhookRestClient;

    @Override
    public void send(
            String url,
            String secret,
            String payload
    ) {
        webhookRestClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        "X-Webhook-Secret",
                        secret
                )
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}