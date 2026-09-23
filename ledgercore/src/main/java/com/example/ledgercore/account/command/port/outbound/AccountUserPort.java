package com.example.ledgercore.account.command.port.outbound;

import java.util.UUID;

public interface AccountUserPort {

    boolean existsById(UUID userId);
}