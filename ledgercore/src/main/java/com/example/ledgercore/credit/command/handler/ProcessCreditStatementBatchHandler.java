package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.service.dto.CreateCreditStatementCommand;
import com.example.ledgercore.credit.command.service.CreateCreditStatementService;
import com.example.ledgercore.credit.command.port.inbound.ProcessCreditStatementBatchUseCase;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessCreditStatementBatchHandler
        implements ProcessCreditStatementBatchUseCase {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private final CreateCreditStatementService
            createCreditStatementService;

    @Override
    @Transactional
    public BatchResult execute(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate statementDate,
            LocalDate dueDate,
            UUID lastProcessedFacilityId,
            int batchSize
    ) {
        validate(
                runId,
                periodStart,
                periodEnd,
                statementDate,
                dueDate,
                batchSize
        );

        List<CreditFacility> facilities =
                creditFacilityCommandRepository.findBatch(
                        lastProcessedFacilityId,
                        PageRequest.of(0, batchSize)
                );

        if (facilities.isEmpty()) {
            return new BatchResult(
                    lastProcessedFacilityId,
                    true
            );
        }

        UUID newLastProcessedFacilityId =
                lastProcessedFacilityId;

        for (CreditFacility facility : facilities) {

            createCreditStatementService.execute(
                    new CreateCreditStatementCommand(
                            runId,
                            facility.getId(),
                            periodStart,
                            periodEnd,
                            statementDate,
                            dueDate
                    )
            );

            newLastProcessedFacilityId =
                    facility.getId();
        }

        boolean completed =
                facilities.size() < batchSize;

        return new BatchResult(
                newLastProcessedFacilityId,
                completed
        );
    }

    private void validate(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate statementDate,
            LocalDate dueDate,
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

        if (statementDate == null) {
            throw new IllegalArgumentException(
                    "statementDate must not be null"
            );
        }

        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "dueDate must not be null"
            );
        }

        if (periodStart.isAfter(periodEnd)
                || periodEnd.isAfter(statementDate)
                || statementDate.isAfter(dueDate)) {
            throw new IllegalArgumentException(
                    "Invalid statement period"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than zero"
            );
        }
    }
}