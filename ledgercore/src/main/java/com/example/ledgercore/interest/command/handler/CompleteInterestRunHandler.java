package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.command.port.inbound.CompleteInterestRunUseCase;
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
public class CompleteInterestRunHandler
        implements CompleteInterestRunUseCase {

    private final InterestRunCommandRepository interestRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            Instant completedAt
    ) {
        if (runId == null || completedAt == null) {
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

        if (run.getStatus() == InterestRunStatus.COMPLETED) {
            throw new BusinessException(
                    ErrorCode.INTEREST_RUN_ALREADY_COMPLETED
            );
        }

        if (run.getStatus() != InterestRunStatus.RUNNING) {
            throw new BusinessException(
                    ErrorCode.INVALID_INTEREST_RUN_STATUS
            );
        }

        run.complete(completedAt);

        interestRunCommandRepository.save(run);
    }
}