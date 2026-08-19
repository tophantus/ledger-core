package com.example.ledgercore.account.command.port.outbound;

import java.util.UUID;

public interface UserAccountPort {

    boolean existsById(UUID userId);
}