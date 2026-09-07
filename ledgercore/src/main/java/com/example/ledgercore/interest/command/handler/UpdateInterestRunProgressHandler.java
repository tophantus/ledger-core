package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.command.port.inbound.UpdateInterestRunProgressUseCase;
import com.example.ledgercore.interest.command.repository.InterestRunCommandRepository;
import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.enums.InterestRunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateInterestRunProgressHandler
        implements UpdateInterestRunProgressUseCase {

    private final InterestRunCommandRepository interestRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            UUID lastProcessedId,
            long processedCount
    ) {
        if (runId == null
                || lastProcessedId == null
                || processedCount < 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        InterestRun run =
                interestRunCommandRepository
                        .findByIdForUpdate(runId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INTEREST_RUN_NOT_FOUND
                                )
                        );

        if (run.getStatus() != InterestRunStatus.RUNNING) {
            throw new BusinessException(
                    ErrorCode.INVALID_INTEREST_RUN_STATUS
            );
        }

        run.updateProgress(
                lastProcessedId,
                processedCount,
                Instant.now()
        );

        interestRunCommandRepository.save(run);
    }
}