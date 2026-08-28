package com.example.ledgercore.account.query.port.inbound;

import java.util.UUID;

public interface GetAccountNoUseCase {

    String execute(UUID accountId);
}