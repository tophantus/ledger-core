package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.AccrueInterestCommand;
import com.example.ledgercore.interest.command.port.inbound.AccrueInterestUseCase;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestAccrualBatchUseCase;
import com.example.ledgercore.interest.command.port.outbound.account.InterestEligibleAccount;
import com.example.ledgercore.interest.command.port.outbound.account.InterestEligibleAccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessInterestAccrualBatchHandler
        implements ProcessInterestAccrualBatchUseCase {

    private final InterestEligibleAccountQueryPort
            eligibleAccountQueryPort;

    private final AccrueInterestUseCase
            accrueInterestUseCase;

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

        List<InterestEligibleAccount> accounts =
                eligibleAccountQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (accounts.isEmpty()) {
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

        for (InterestEligibleAccount account : accounts) {

            accrueInterestUseCase.execute(
                    new AccrueInterestCommand(
                            runId,
                            account.accountId(),
                            account.productId(),
                            account.currency(),
                            businessDate
                    )
            );

            newLastProcessedId =
                    account.accountId();

            newProcessedCount++;
        }

        boolean completed =
                accounts.size() < batchSize;

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
            throw new IllegalArgumentException("runId must not be null");
        }

        if (businessDate == null) {
            throw new IllegalArgumentException("businessDate must not be null");
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