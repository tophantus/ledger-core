package com.example.ledgercore.webhook.adapter.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test/webhook")
public class TestWebhookController {

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String body,
            @RequestHeader(value = "X-Webhook-Event", required = false)
            String eventType
    ) {

        log.info("========== WEBHOOK RECEIVED ==========");
        log.info("Event Type: {}", eventType);
        log.info("Payload: {}", body);
        log.info("======================================");

        return ResponseEntity.ok().build();
    }
}