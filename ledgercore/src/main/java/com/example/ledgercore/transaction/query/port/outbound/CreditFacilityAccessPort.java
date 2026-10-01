package com.example.ledgercore.transaction.query.port.outbound;

import java.util.UUID;

public interface CreditFacilityAccessPort {

    void verifyAccess(
            UUID userId,
            UUID creditFacilityId
    );
}