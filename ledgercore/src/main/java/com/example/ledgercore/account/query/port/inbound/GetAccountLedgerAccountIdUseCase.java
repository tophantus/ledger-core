package com.example.ledgercore.account.query.port.inbound;

import java.util.UUID;

public interface GetAccountLedgerAccountIdUseCase {

    UUID execute(UUID accountId);
}