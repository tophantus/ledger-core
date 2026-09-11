package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestPostingBatchUseCase;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.config.InterestRunProperties;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostingInterestRunProcessor
        implements InterestRunProcessor {

    private final ProcessInterestPostingBatchUseCase
            processBatchUseCase;

    private final UpdateInterestRunProgressUseCase
            updateProgressUseCase;

    private final CompleteInterestRunUseCase
            completeRunUseCase;

    private final InterestRunProperties
            interestRunProperties;

    @Override
    public InterestRunType getType() {
        return InterestRunType.POSTING;
    }

    @Override
    public void process(ClaimedInterestRun run) {

        UUID runId = run.runId();

        UUID lastProcessedId =
                run.lastProcessedId();

        long processedCount =
                run.processedCount();

        LocalDate periodEnd =
                run.businessDate();

        LocalDate periodStart =
                periodEnd.withDayOfMonth(1);

        log.info(
                "Starting interest posting run: runId={}, periodStart={}, periodEnd={}, lastProcessedId={}, processedCount={}",
                runId,
                periodStart,
                periodEnd,
                lastProcessedId,
                processedCount
        );

        while (true) {

            ProcessInterestPostingBatchUseCase.BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            periodStart,
                            periodEnd,
                            lastProcessedId,
                            processedCount,
                            interestRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed interest posting batch: runId={}, periodStart={}, periodEnd={}, lastProcessedId={}, processedCount={}, completed={}",
                    runId,
                    periodStart,
                    periodEnd,
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
                        "Completed interest posting run: runId={}, periodStart={}, periodEnd={}, processedCount={}",
                        runId,
                        periodStart,
                        periodEnd,
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