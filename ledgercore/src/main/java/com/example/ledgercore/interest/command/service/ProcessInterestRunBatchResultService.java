package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.dto.InterestRunBatchResult;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.CreateInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.enums.InterestRunType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessInterestRunBatchResultService {

    private final UpdateInterestRunProgressUseCase
            updateProgressUseCase;

    private final CompleteInterestRunUseCase
            completeRunUseCase;

    private final CreateInterestRunUseCase
            createInterestRunUseCase;

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

        if (isEndOfMonthAccrual(run)) {
            createInterestRunUseCase.execute(
                    run.businessDate(),
                    InterestRunType.POSTING
            );
        }
    }

    private boolean isEndOfMonthAccrual(
            ClaimedInterestRun run
    ) {
        return run.runType() == InterestRunType.ACCRUAL
                && run.businessDate().getDayOfMonth()
                == run.businessDate().lengthOfMonth();
    }
}