package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.credit.command.dto.ClaimedCreditOfferRun;
import com.example.ledgercore.credit.command.port.inbound.CompleteCreditOfferRunUseCase;
import com.example.ledgercore.credit.command.port.inbound.ProcessCreditOfferBatchUseCase;
import com.example.ledgercore.credit.command.port.inbound.UpdateCreditOfferRunProgressUseCase;
import com.example.ledgercore.credit.command.service.CreditOfferRunProcessor;
import com.example.ledgercore.credit.config.CreditOfferRunProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditOfferRunProcessorImpl
        implements CreditOfferRunProcessor {

    private final ProcessCreditOfferBatchUseCase
            processBatchUseCase;

    private final UpdateCreditOfferRunProgressUseCase
            updateProgressUseCase;

    private final CompleteCreditOfferRunUseCase
            completeRunUseCase;

    private final CreditOfferRunProperties
            creditOfferRunProperties;

    @Override
    public void process(ClaimedCreditOfferRun run) {

        UUID runId = run.runId();
        UUID lastProcessedId = run.lastProcessedId();

        log.info(
                "Starting credit offer run: runId={}, businessDate={}, lastProcessedId={}",
                runId,
                run.businessDate(),
                lastProcessedId
        );

        while (true) {

            ProcessCreditOfferBatchUseCase.BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.businessDate(),
                            lastProcessedId,
                            creditOfferRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed credit offer batch: runId={}, businessDate={}, lastProcessedId={}, completed={}",
                    runId,
                    run.businessDate(),
                    result.lastProcessedId(),
                    result.completed()
            );

            updateProgressUseCase.execute(
                    runId,
                    result.lastProcessedId(),
                    Instant.now()
            );

            if (result.completed()) {

                completeRunUseCase.execute(
                        runId,
                        Instant.now()
                );

                log.info(
                        "Completed credit offer run: runId={}, businessDate={}",
                        runId,
                        run.businessDate()
                );

                return;
            }

            lastProcessedId =
                    result.lastProcessedId();
        }
    }
}