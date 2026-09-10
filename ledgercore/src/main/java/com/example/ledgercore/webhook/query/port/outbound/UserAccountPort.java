package com.example.ledgercore.webhook.query.port.outbound;

import java.util.List;
import java.util.UUID;

public interface UserAccountPort {

    List<UUID> getAccountIds(UUID userId);
}