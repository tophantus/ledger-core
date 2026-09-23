package com.example.ledgercore.credit.query.port.inbound;

import java.util.UUID;

public interface GetCreditFacilityLedgerAccountUseCase {

    UUID execute(UUID creditFacilityId);
}