package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.credit.command.port.inbound.ProcessCreditStatementBusinessDayClosedUseCase;
import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.dto.InterestRunBatchResult;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.CreateInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.command.service.ProcessInterestRunBatchResultService;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessInterestRunBatchResultServiceImpl
        implements ProcessInterestRunBatchResultService {

    private final UpdateInterestRunProgressUseCase
            updateProgressUseCase;

    private final CompleteInterestRunUseCase
            completeRunUseCase;

    private final CreateInterestRunUseCase
            createInterestRunUseCase;

    private final ProcessCreditStatementBusinessDayClosedUseCase
            processCreditStatementBusinessDayClosedUseCase;

    @Override
    @Transactional
    public void process(
            ClaimedInterestRun run,
            InterestRunBatchResult result
    ) {
        updateProgressUseCase.execute(
                run.runId(),
                result.lastProcessedId(),
                result.processedCount()
        );

        if (!result.completed()) {
            return;
        }

        completeRunUseCase.execute(
                run.runId(),
                Instant.now()
        );

        if (!isEndOfMonth(run)) {
            return;
        }

        if (run.runType() == InterestRunType.ACCRUAL) {
            createInterestRunUseCase.execute(
                    run.businessDate(),
                    InterestRunType.POSTING
            );
            return;
        }

        if (run.runType() == InterestRunType.CREDIT_ACCRUAL) {
            processCreditStatementBusinessDayClosedUseCase.execute(
                    run.businessDate()
            );
        }
    }

    private boolean isEndOfMonth(ClaimedInterestRun run) {
        return run.businessDate().getDayOfMonth()
                == run.businessDate().lengthOfMonth();
    }
}