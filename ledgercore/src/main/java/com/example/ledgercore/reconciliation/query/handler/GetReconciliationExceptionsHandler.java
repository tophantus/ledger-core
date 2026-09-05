package com.example.ledgercore.reconciliation.query.handler;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationExceptionResponse;
import com.example.ledgercore.reconciliation.query.port.inbound.GetReconciliationExceptionsUseCase;
import com.example.ledgercore.reconciliation.query.repository.ReconciliationExceptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetReconciliationExceptionsHandler
        implements GetReconciliationExceptionsUseCase {

    private final ReconciliationExceptionQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationExceptionResponse> execute(
            LocalDate businessDate,
            ReconciliationTargetType targetType,
            ReconciliationErrorCode errorCode,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return repository
                .findAllForAdmin(
                        businessDate,
                        targetType,
                        errorCode,
                        pageable
                )
                .map(data ->
                        new ReconciliationExceptionResponse(
                                data.getId(),
                                data.getReconciliationRunId(),
                                data.getBusinessDate(),
                                data.getTargetType(),
                                data.getTargetId(),
                                data.getErrorCode(),
                                data.getExpectedValue(),
                                data.getActualValue(),
                                data.getMessage(),
                                data.getCreatedAt()
                        )
                );
    }
}