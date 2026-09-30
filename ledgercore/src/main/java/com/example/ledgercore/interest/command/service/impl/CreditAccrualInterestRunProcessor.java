package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestCreditAccrualBatchUseCase;
import com.example.ledgercore.interest.command.service.InterestRunProcessor;
import com.example.ledgercore.interest.command.service.ProcessInterestRunBatchResultService;
import com.example.ledgercore.interest.config.InterestRunProperties;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditAccrualInterestRunProcessor
        implements InterestRunProcessor {

    private final ProcessInterestCreditAccrualBatchUseCase
            processBatchUseCase;

    private final ProcessInterestRunBatchResultService
            processBatchResultService;

    private final InterestRunProperties
            interestRunProperties;

    @Override
    public InterestRunType getType() {
        return InterestRunType.CREDIT_ACCRUAL;
    }

    @Override
    public void process(ClaimedInterestRun run) {

        UUID runId = run.runId();
        UUID lastProcessedId = run.lastProcessedId();
        long processedCount = run.processedCount();

        log.info(
                "Starting credit interest accrual run: runId={}, businessDate={}, lastProcessedId={}, processedCount={}",
                runId,
                run.businessDate(),
                lastProcessedId,
                processedCount
        );

        while (true) {

            ProcessInterestCreditAccrualBatchUseCase.BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.businessDate(),
                            lastProcessedId,
                            processedCount,
                            interestRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed credit interest accrual batch: runId={}, businessDate={}, lastProcessedId={}, processedCount={}, completed={}",
                    runId,
                    run.businessDate(),
                    result.lastProcessedId(),
                    result.processedCount(),
                    result.completed()
            );

            processBatchResultService.process(
                    run,
                    result
            );

            if (result.completed()) {

                log.info(
                        "Completed credit interest accrual run: runId={}, businessDate={}, processedCount={}",
                        runId,
                        run.businessDate(),
                        result.processedCount()
                );

                return;
            }

            lastProcessedId =
                    result.lastProcessedId();

            processedCount =
                    result.processedCount();
        }
    }
}