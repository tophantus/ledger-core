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

    private static final int MAX_RESPONSE_BODY_LENGTH = 2000;

    private final RestClient restClient;

    @Override
    public WebhookResponse send(
            String url,
            String secret,
            String payload
    ) {
        try {
            return restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Webhook-Secret", secret)
                    .body(payload)
                    .exchange((request, response) -> {

                        String body = null;

                        if (response.getBody() != null) {
                            body = new String(
                                    response.getBody().readAllBytes()
                            );
                        }

                        return new WebhookResponse(
                                response.getStatusCode().value(),
                                truncate(body),
                                null
                        );
                    });

        } catch (Exception ex) {
            return new WebhookResponse(
                    0,
                    null,
                    truncate(ex.getMessage())
            );
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }

        return value.length() <= MAX_RESPONSE_BODY_LENGTH
                ? value
                : value.substring(
                0,
                MAX_RESPONSE_BODY_LENGTH
        );
    }
}