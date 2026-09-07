package com.example.ledgercore.reconciliation.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationExceptionResponse;

import java.time.LocalDate;

public interface GetReconciliationExceptionsUseCase {

    PageResponse<ReconciliationExceptionResponse> execute(
            LocalDate businessDate,
            ReconciliationTargetType targetType,
            ReconciliationErrorCode errorCode,
            int page,
            int size
    );
}