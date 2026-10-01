package com.example.ledgercore.credit.command.port.inbound;

import java.util.UUID;

public interface RepayCreditFacilityUseCase {

    void execute(UUID statementId);
}