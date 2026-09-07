package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.DispatchInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestAccrualBatchUseCase;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.config.InterestRunProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchInterestRunHandler
        implements DispatchInterestRunUseCase {

    private final ProcessInterestAccrualBatchUseCase processBatchUseCase;
    private final UpdateInterestRunProgressUseCase updateProgressUseCase;
    private final CompleteInterestRunUseCase completeInterestRunUseCase;
    private final InterestRunProperties properties;

    @Override
    public void execute(ClaimedInterestRun run) {

        log.info(
                "Starting interest run: runId={}, businessDate={}, lastProcessedId={}, processedCount={}",
                run.runId(),
                run.businessDate(),
                run.lastProcessedId(),
                run.processedCount()
        );

        UUID lastProcessedId = run.lastProcessedId();
        long processedCount = run.processedCount();

        while (true) {

            ProcessInterestAccrualBatchUseCase.BatchResult result =
                    processBatchUseCase.execute(
                            run.runId(),
                            run.businessDate(),
                            lastProcessedId,
                            processedCount,
                            properties.getBatchSize()
                    );

            lastProcessedId = result.lastProcessedId();
            processedCount = result.processedCount();

            log.info(
                    "Interest batch processed: runId={}, lastProcessedId={}, processedCount={}, completed={}",
                    run.runId(),
                    lastProcessedId,
                    processedCount,
                    result.completed()
            );

            updateProgressUseCase.execute(
                    run.runId(),
                    lastProcessedId,
                    processedCount
            );

            if (result.completed()) {

                Instant completedAt = Instant.now();

                completeInterestRunUseCase.execute(
                        run.runId(),
                        completedAt
                );

                log.info(
                        "Interest run completed: runId={}, businessDate={}, processedCount={}",
                        run.runId(),
                        run.businessDate(),
                        processedCount
                );

                return;
            }
        }
    }
}