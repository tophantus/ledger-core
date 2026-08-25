package com.example.ledgercore.webhook.command.port.outbound;

public interface WebhookHttpClientPort {

    WebhookResponse send(
            String url,
            String secret,
            String payload
    );

    record WebhookResponse(
            int statusCode,
            String responseBody,
            String error
    ) {

        public boolean isSuccess() {
            return statusCode >= 200
                    && statusCode < 300;
        }

        public boolean isRetryable() {
            return statusCode == 408
                    || statusCode == 429
                    || statusCode >= 500
                    || statusCode == 0;
        }
    }
}