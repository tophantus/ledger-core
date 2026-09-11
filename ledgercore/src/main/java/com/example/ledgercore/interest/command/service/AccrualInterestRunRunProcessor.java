package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestAccrualBatchUseCase;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.config.InterestRunProperties;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccrualInterestRunRunProcessor
        implements InterestRunProcessor {

    private final ProcessInterestAccrualBatchUseCase
            processBatchUseCase;

    private final UpdateInterestRunProgressUseCase
            updateProgressUseCase;

    private final CompleteInterestRunUseCase
            completeRunUseCase;

    private final InterestRunProperties
            interestRunProperties;

    @Override
    public InterestRunType getType() {
        return InterestRunType.ACCRUAL;
    }

    @Override
    public void process(ClaimedInterestRun run) {

        UUID runId = run.runId();
        UUID lastProcessedId = run.lastProcessedId();
        long processedCount = run.processedCount();

        log.info(
                "Starting interest accrual run: runId={}, businessDate={}, lastProcessedId={}, processedCount={}",
                runId,
                run.businessDate(),
                lastProcessedId,
                processedCount
        );

        while (true) {

            ProcessInterestAccrualBatchUseCase.BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.businessDate(),
                            lastProcessedId,
                            processedCount,
                            interestRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed interest accrual batch: runId={}, businessDate={}, lastProcessedId={}, processedCount={}, completed={}",
                    runId,
                    run.businessDate(),
                    result.lastProcessedId(),
                    result.processedCount(),
                    result.completed()
            );

            updateProgressUseCase.execute(
                    runId,
                    result.lastProcessedId(),
                    result.processedCount()
            );

            if (result.completed()) {

                completeRunUseCase.execute(
                        runId,
                        Instant.now()
                );

                log.info(
                        "Completed interest accrual run: runId={}, businessDate={}, processedCount={}",
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