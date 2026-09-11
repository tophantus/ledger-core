package com.example.ledgercore.webhook.query.port.outbound;

import java.util.List;
import java.util.UUID;

public interface WebhookUserAccountPort {

    List<UUID> getAccountIds(UUID userId);
}