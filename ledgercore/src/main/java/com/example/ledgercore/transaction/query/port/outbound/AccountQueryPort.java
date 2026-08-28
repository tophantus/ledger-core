package com.example.ledgercore.transaction.query.port.outbound;

import java.util.UUID;

public interface AccountQueryPort {

    String getAccountNoByAccountId(
            UUID accountId
    );
}