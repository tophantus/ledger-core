package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.EvaluateAndCreateCreditOfferUseCase;
import com.example.ledgercore.credit.command.port.inbound.ProcessCreditOfferBatchUseCase;
import com.example.ledgercore.credit.command.port.outbound.CreditOfferCandidateQueryPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditOfferCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessCreditOfferBatchHandler
        implements ProcessCreditOfferBatchUseCase {

    private final CreditOfferCandidateQueryPort
            creditOfferCandidateQueryPort;

    private final EvaluateAndCreateCreditOfferUseCase
            evaluateAndCreateCreditOfferUseCase;

    @Override
    @Transactional
    public BatchResult execute(
            UUID runId,
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    ) {
        validate(
                runId,
                businessDate,
                batchSize
        );

        List<CreditOfferCandidate> candidates =
                creditOfferCandidateQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (candidates.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    true
            );
        }

        UUID newLastProcessedId =
                lastProcessedId;

        for (CreditOfferCandidate candidate : candidates) {

            evaluateAndCreateCreditOfferUseCase.execute(
                    runId,
                    candidate.customerId()
            );

            newLastProcessedId =
                    candidate.customerId();
        }

        boolean completed =
                candidates.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                completed
        );
    }

    private void validate(
            UUID runId,
            LocalDate businessDate,
            int batchSize
    ) {
        if (runId == null) {
            throw new IllegalArgumentException(
                    "runId must not be null"
            );
        }

        if (businessDate == null) {
            throw new IllegalArgumentException(
                    "businessDate must not be null"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}