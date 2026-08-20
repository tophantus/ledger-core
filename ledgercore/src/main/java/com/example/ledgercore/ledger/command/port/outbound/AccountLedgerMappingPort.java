package com.example.ledgercore.ledger.command.port.outbound;

import java.util.UUID;

public interface AccountLedgerMappingPort {

    UUID getLedgerAccountId(UUID accountId);
}