package com.example.ledgercore.interest.command.port.outbound.account;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InterestEligibleAccountQueryPort {

    List<InterestEligibleAccount> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );
}