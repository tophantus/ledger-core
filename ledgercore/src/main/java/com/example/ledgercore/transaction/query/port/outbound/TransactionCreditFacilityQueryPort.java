package com.example.ledgercore.transaction.query.port.outbound;

import java.util.Optional;
import java.util.UUID;

public interface TransactionCreditFacilityQueryPort {

    Optional<UUID> findCreditFacilityIdByUserId(UUID userId);
}