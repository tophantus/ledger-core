package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.reconciliation.command.port.inbound.RecordReconciliationExceptionUseCase;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationExceptionCommandRepository;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.entity.ReconciliationException;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordReconciliationExceptionHandler
        implements RecordReconciliationExceptionUseCase {

    private final ReconciliationExceptionCommandRepository
            exceptionRepository;

    private final ReconciliationRunCommandRepository
            runRepository;

    @Override
    @Transactional
    public void execute(
            UUID reconciliationRunId,
            ReconciliationTargetType targetType,
            UUID targetId,
            ReconciliationErrorCode errorCode,
            String expectedValue,
            String actualValue,
            String message
    ) {

        ReconciliationRun run =
                runRepository.findById(reconciliationRunId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RECONCILIATION_RUN_NOT_FOUND
                                )
                        );

        ReconciliationException exception =
                ReconciliationException.builder()
                        .reconciliationRun(run)
                        .targetType(targetType)
                        .targetId(targetId)
                        .errorCode(errorCode)
                        .expectedValue(expectedValue)
                        .actualValue(actualValue)
                        .message(message)
                        .build();

        exceptionRepository.save(exception);
    }
}