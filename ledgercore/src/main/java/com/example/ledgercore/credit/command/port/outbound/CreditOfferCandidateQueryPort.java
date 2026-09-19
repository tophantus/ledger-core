package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.CreditOfferCandidate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditOfferCandidateQueryPort {

    List<CreditOfferCandidate> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );
}