package com.example.ledgercore.account.query.port.outbound;

import java.util.UUID;

public interface AccountHolderProfilePort {

    String getFullName(UUID userId);
}