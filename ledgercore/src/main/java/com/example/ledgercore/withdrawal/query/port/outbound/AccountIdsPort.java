package com.example.ledgercore.withdrawal.query.port.outbound;

import java.util.List;
import java.util.UUID;

public interface AccountIdsPort {

    List<UUID> getAccountIdsByUser(
            UUID userId
    );
}