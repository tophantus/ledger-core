package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditStatementRun;
import com.example.ledgercore.credit.command.service.ProcessCreditStatementBatchService;
import com.example.ledgercore.credit.command.port.inbound.run.CompleteCreditStatementRunUseCase;
import com.example.ledgercore.credit.command.port.inbound.run.UpdateCreditStatementRunProgressUseCase;
import com.example.ledgercore.credit.command.port.inbound.ProcessCreditStatementRunUseCase;
import com.example.ledgercore.credit.config.CreditStatementRunProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCreditStatementRunHandler
        implements ProcessCreditStatementRunUseCase {

    private final ProcessCreditStatementBatchService
            processBatchUseCase;

    private final UpdateCreditStatementRunProgressUseCase
            updateProgressUseCase;

    private final CompleteCreditStatementRunUseCase
            completeRunUseCase;

    private final CreditStatementRunProperties
            creditStatementRunProperties;

    @Override
    public void process(ClaimedCreditStatementRun run) {

        UUID runId = run.runId();
        UUID lastProcessedFacilityId =
                run.lastProcessedFacilityId();

        log.info(
                "Starting credit statement run: runId={}, periodStart={}, periodEnd={}, lastProcessedFacilityId={}",
                runId,
                run.periodStart(),
                run.periodEnd(),
                lastProcessedFacilityId
        );

        while (true) {

            ProcessCreditStatementBatchService.BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.periodStart(),
                            run.periodEnd(),
                            run.statementDate(),
                            run.dueDate(),
                            lastProcessedFacilityId,
                            creditStatementRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed credit statement batch: runId={}, periodStart={}, periodEnd={}, lastProcessedFacilityId={}, completed={}",
                    runId,
                    run.periodStart(),
                    run.periodEnd(),
                    result.lastProcessedFacilityId(),
                    result.completed()
            );

            updateProgressUseCase.execute(
                    runId,
                    result.lastProcessedFacilityId(),
                    Instant.now()
            );

            if (result.completed()) {

                completeRunUseCase.execute(
                        runId,
                        Instant.now()
                );

                log.info(
                        "Completed credit statement run: runId={}, periodStart={}, periodEnd={}",
                        runId,
                        run.periodStart(),
                        run.periodEnd()
                );

                return;
            }

            lastProcessedFacilityId =
                    result.lastProcessedFacilityId();
        }
    }
}