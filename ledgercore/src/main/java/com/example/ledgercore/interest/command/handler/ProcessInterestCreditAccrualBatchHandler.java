package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.AccrueCreditInterestCommand;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestCreditAccrualBatchUseCase;
import com.example.ledgercore.interest.command.port.outbound.credit.InterestEligibleCreditFacility;
import com.example.ledgercore.interest.command.port.outbound.credit.InterestEligibleCreditFacilityQueryPort;
import com.example.ledgercore.interest.command.service.AccrueCreditInterestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessInterestCreditAccrualBatchHandler
        implements ProcessInterestCreditAccrualBatchUseCase {

    private final InterestEligibleCreditFacilityQueryPort
            eligibleCreditFacilityQueryPort;

    private final AccrueCreditInterestUseCase
            accrueCreditInterestUseCase;

    @Override
    @Transactional
    public BatchResult execute(
            UUID runId,
            LocalDate businessDate,
            UUID lastProcessedId,
            long processedCount,
            int batchSize
    ) {

        validate(
                runId,
                businessDate,
                processedCount,
                batchSize
        );

        List<InterestEligibleCreditFacility> facilities =
                eligibleCreditFacilityQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (facilities.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    processedCount,
                    true
            );
        }

        UUID newLastProcessedId =
                lastProcessedId;

        long newProcessedCount =
                processedCount;

        for (InterestEligibleCreditFacility facility : facilities) {

            accrueCreditInterestUseCase.execute(
                    new AccrueCreditInterestCommand(
                            runId,
                            facility.creditFacilityId(),
                            facility.productId(),
                            facility.currency(),
                            businessDate
                    )
            );

            newLastProcessedId =
                    facility.creditFacilityId();

            newProcessedCount++;
        }

        boolean completed =
                facilities.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                completed
        );
    }

    private void validate(
            UUID runId,
            LocalDate businessDate,
            long processedCount,
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

        if (processedCount < 0) {
            throw new IllegalArgumentException(
                    "processedCount must not be negative"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}