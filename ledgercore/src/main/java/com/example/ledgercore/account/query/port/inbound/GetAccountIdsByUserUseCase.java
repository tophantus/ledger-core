package com.example.ledgercore.account.query.port.inbound;

import java.util.List;
import java.util.UUID;

public interface GetAccountIdsByUserUseCase {

    List<UUID> execute(
            UUID userId
    );
}