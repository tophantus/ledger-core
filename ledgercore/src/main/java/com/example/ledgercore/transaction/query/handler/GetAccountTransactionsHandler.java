package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.GetAccountTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.outbound.TransactionAccessPort;
import com.example.ledgercore.transaction.query.repository.TransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountTransactionsHandler
        implements GetAccountTransactionsUseCase {

    private final TransactionQueryRepository transactionQueryRepository;
    private final TransactionAccessPort transactionAccessPort;
    private final TransactionQueryMapper transactionQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> execute(
            GetAccountTransactionsQuery query
    ) {
        validateQuery(query);

        transactionAccessPort.verifyAccess(
                query.userId(),
                query.accountId()
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
                        .map(transactionQueryMapper::toResponse)
                        .toList(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    private Specification<MoneyTransaction> buildSpecification(
            GetAccountTransactionsQuery query
    ) {
        Specification<MoneyTransaction> specification =
                accountSpecification(query.accountId());

        if (query.status() != null) {
            specification = specification.and(
                    statusSpecification(query.status())
            );
        }

        if (query.type() != null) {
            specification = specification.and(
                    typeSpecification(query.type())
            );
        }

        if (query.currency() != null
                && !query.currency().isBlank()) {

            specification = specification.and(
                    currencySpecification(query.currency())
            );
        }

        if (query.from() != null) {
            specification = specification.and(
                    createdAtGreaterThanOrEqualTo(query.from())
            );
        }

        if (query.to() != null) {
            specification = specification.and(
                    createdAtLessThanOrEqualTo(query.to())
            );
        }

        return specification;
    }

    private Specification<MoneyTransaction> accountSpecification(
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.equal(
                                root.get("sourceAccountId"),
                                accountId
                        ),
                        criteriaBuilder.equal(
                                root.get("destinationAccountId"),
                                accountId
                        )
                );
    }

    private Specification<MoneyTransaction> statusSpecification(
            com.example.ledgercore.transaction.enums.TransactionStatus status
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    private Specification<MoneyTransaction> typeSpecification(
            com.example.ledgercore.transaction.enums.TransactionType type
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("type"),
                        type
                );
    }

    private Specification<MoneyTransaction> currencySpecification(
            String currency
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("currency"),
                        currency
                );
    }

    private Specification<MoneyTransaction> createdAtGreaterThanOrEqualTo(
            Instant from
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    private Specification<MoneyTransaction> createdAtLessThanOrEqualTo(
            Instant to
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );
    }

    private void validateQuery(
            GetAccountTransactionsQuery query
    ) {
        if (query == null
                || query.userId() == null
                || query.accountId() == null) {

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

        if (query.currency() != null
                && query.currency().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}