package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.PostInterestCommand;
import com.example.ledgercore.interest.command.port.inbound.PostInterestUseCase;
import com.example.ledgercore.interest.command.port.inbound.ProcessInterestPostingBatchUseCase;
import com.example.ledgercore.interest.query.repository.InterestAccrualQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessInterestPostingBatchHandler
        implements ProcessInterestPostingBatchUseCase {

    private final InterestAccrualQueryRepository
            interestAccrualQueryRepository;

    private final PostInterestUseCase
            postInterestUseCase;

    @Override
    @Transactional
    public BatchResult execute(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
            UUID lastProcessedId,
            long processedCount,
            int batchSize
    ) {
        validate(
                runId,
                periodStart,
                periodEnd,
                processedCount,
                batchSize
        );

        List<UUID> accountIds =
                interestAccrualQueryRepository
                        .findDistinctAccountIds(
                                periodStart,
                                periodEnd,
                                lastProcessedId,
                                PageRequest.of(0, batchSize)
                        );

        if (accountIds.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    processedCount,
                    true
            );
        }

        UUID newLastProcessedId =
                lastProcessedId;

        long newProcessedCount =
                processedCount;

        for (UUID accountId : accountIds) {
            postInterestUseCase.execute(
                    new PostInterestCommand(
                            runId,
                            accountId,
                            periodStart,
                            periodEnd
                    )
            );

            newLastProcessedId = accountId;
            newProcessedCount++;
        }

        boolean completed =
                accountIds.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                completed
        );
    }

    private void validate(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
            long processedCount,
            int batchSize
    ) {
        if (runId == null) {
            throw new IllegalArgumentException(
                    "runId must not be null"
            );
        }

        if (periodStart == null) {
            throw new IllegalArgumentException(
                    "periodStart must not be null"
            );
        }

        if (periodEnd == null) {
            throw new IllegalArgumentException(
                    "periodEnd must not be null"
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException(
                    "periodStart must not be after periodEnd"
            );
        }

        if (processedCount < 0) {
            throw new IllegalArgumentException(
                    "processedCount must not be negative"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}