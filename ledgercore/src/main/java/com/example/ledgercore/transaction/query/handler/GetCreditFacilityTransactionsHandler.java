package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.GetCreditFacilityTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.inbound.GetCreditFacilityTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.outbound.CreditFacilityAccessPort;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import com.example.ledgercore.transaction.query.specification.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreditFacilityTransactionsHandler
        implements GetCreditFacilityTransactionsUseCase {

    private final TransactionQueryRepository transactionQueryRepository;

    private final CreditFacilityAccessPort creditFacilityAccessPort;

    private final TransactionQueryMapper transactionQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> execute(
            GetCreditFacilityTransactionsQuery query
    ) {
        validateQuery(query);

        creditFacilityAccessPort.verifyAccess(
                query.userId(),
                query.creditFacilityId()
        );

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Specification<MoneyTransaction> specification =
                buildSpecification(query);

        Page<MoneyTransaction> transactionPage =
                transactionQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                transactionPage.getContent()
                        .stream()
                        .map(transaction ->
                                transactionQueryMapper.toResponse(
                                        transaction,
                                        isIncoming(
                                                transaction,
                                                query.creditFacilityId()
                                        )
                                )
                        )
                        .toList(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    private boolean isIncoming(
            MoneyTransaction transaction,
            UUID creditFacilityId
    ) {
        return creditFacilityId.equals(
                transaction.getDestinationCreditFacilityId()
        );
    }

    private Specification<MoneyTransaction> buildSpecification(
            GetCreditFacilityTransactionsQuery query
    ) {
        Specification<MoneyTransaction> specification =
                TransactionSpecifications.creditFacility(
                        query.creditFacilityId()
                );

        if (query.status() != null) {
            specification = specification.and(
                    TransactionSpecifications.status(
                            query.status()
                    )
            );
        }

        if (query.type() != null) {
            specification = specification.and(
                    TransactionSpecifications.type(
                            query.type()
                    )
            );
        }

        if (query.currency() != null) {
            specification = specification.and(
                    TransactionSpecifications.currency(
                            query.currency()
                    )
            );
        }

        if (query.from() != null) {
            specification = specification.and(
                    TransactionSpecifications.createdAtFrom(
                            query.from()
                    )
            );
        }

        if (query.to() != null) {
            specification = specification.and(
                    TransactionSpecifications.createdAtTo(
                            query.to()
                    )
            );
        }

        return specification;
    }

    private void validateQuery(
            GetCreditFacilityTransactionsQuery query
    ) {
        if (query == null
                || query.userId() == null
                || query.creditFacilityId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.page() < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.size() <= 0
                || query.size() > 100) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.from() != null
                && query.to() != null
                && query.from().isAfter(query.to())) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}