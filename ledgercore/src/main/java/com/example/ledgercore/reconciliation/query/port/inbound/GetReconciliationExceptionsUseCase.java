package com.example.ledgercore.reconciliation.query.port.inbound;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationExceptionResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface GetReconciliationExceptionsUseCase {

    Page<ReconciliationExceptionResponse> execute(
            LocalDate businessDate,
            ReconciliationTargetType targetType,
            ReconciliationErrorCode errorCode,
            int page,
            int size
    );
}