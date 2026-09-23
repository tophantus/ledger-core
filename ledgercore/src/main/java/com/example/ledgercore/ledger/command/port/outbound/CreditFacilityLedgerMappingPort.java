package com.example.ledgercore.ledger.command.port.outbound;

import java.util.UUID;

public interface CreditFacilityLedgerMappingPort {

    UUID getLedgerAccountId(UUID creditFacilityId);
}