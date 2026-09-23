package com.example.ledgercore.account.command.port.outbound;

import java.util.UUID;

public interface AccountProviderPort {

    boolean existsById(UUID providerId);
}