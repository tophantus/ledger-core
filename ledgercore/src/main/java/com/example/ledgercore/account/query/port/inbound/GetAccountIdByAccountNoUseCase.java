package com.example.ledgercore.account.query.port.inbound;

import java.util.UUID;

public interface GetAccountIdByAccountNoUseCase {

    UUID execute(String accountNo);
}