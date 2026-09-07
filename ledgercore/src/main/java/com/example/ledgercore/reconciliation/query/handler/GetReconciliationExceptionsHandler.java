package com.example.ledgercore.reconciliation.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.reconciliation.entity.ReconciliationException;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import com.example.ledgercore.reconciliation.query.dto.ReconciliationExceptionResponse;
import com.example.ledgercore.reconciliation.query.port.inbound.GetReconciliationExceptionsUseCase;
import com.example.ledgercore.reconciliation.query.repository.ReconciliationExceptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PageResponse<ReconciliationExceptionResponse> execute(
            LocalDate businessDate,
            ReconciliationTargetType targetType,
            ReconciliationErrorCode errorCode,
            int page,
            int size
    ) {

        validatePagination(page, size);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Specification<ReconciliationException> specification =
                buildSpecification(
                        businessDate,
                        targetType,
                        errorCode
                );

        Page<ReconciliationExceptionResponse> result =
                repository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(exception ->
                                new ReconciliationExceptionResponse(
                                        exception.getId(),
                                        exception
                                                .getReconciliationRun()
                                                .getId(),
                                        exception
                                                .getReconciliationRun()
                                                .getBusinessDate(),
                                        exception.getTargetType(),
                                        exception.getTargetId(),
                                        exception.getErrorCode(),
                                        exception.getExpectedValue(),
                                        exception.getActualValue(),
                                        exception.getMessage(),
                                        exception.getCreatedAt()
                                )
                        );

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private Specification<ReconciliationException>
    buildSpecification(
            LocalDate businessDate,
            ReconciliationTargetType targetType,
            ReconciliationErrorCode errorCode
    ) {

        Specification<ReconciliationException> specification =
                (root, query, cb) ->
                        cb.conjunction();

        if (businessDate != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root
                                            .get("reconciliationRun")
                                            .get("businessDate"),
                                    businessDate
                            )
            );
        }

        if (targetType != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("targetType"),
                                    targetType
                            )
            );
        }

        if (errorCode != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("errorCode"),
                                    errorCode
                            )
            );
        }

        return specification;
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must not be negative"
            );
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100"
            );
        }
    }
}