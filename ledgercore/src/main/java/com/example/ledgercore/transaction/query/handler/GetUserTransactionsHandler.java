package com.example.ledgercore.transaction.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.query.dto.GetUserTransactionsQuery;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transaction.query.mapper.TransactionQueryMapper;
import com.example.ledgercore.transaction.query.port.inbound.GetUserTransactionsUseCase;
import com.example.ledgercore.transaction.query.port.outbound.AccountQueryPort;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserTransactionsHandler
        implements GetUserTransactionsUseCase {

    private final TransactionQueryRepository transactionQueryRepository;
    private final AccountQueryPort accountQueryPort;
    private final TransactionQueryMapper transactionQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> execute(
            GetUserTransactionsQuery query
    ) {
        validateQuery(query);

        List<UUID> accountIds =
                accountQueryPort.findAccountIdsByUserId(
                        query.userId()
                );

        if (accountIds.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    query.page(),
                    query.size(),
                    0,
                    0
            );
        }

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Specification<MoneyTransaction> specification =
                buildSpecification(
                        accountIds,
                        query
                );

        Page<MoneyTransaction> transactionPage =
                transactionQueryRepository.findAll(
                        specification,
                        pageable
                );

        Set<UUID> accountIdSet = new HashSet<>(accountIds);

        return new PageResponse<>(
                transactionPage.getContent()
                        .stream()
                        .map(transaction ->
                                transactionQueryMapper.toResponse(
                                        transaction,
                                        resolveIncoming(
                                                transaction,
                                                accountIdSet
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

    private Boolean resolveIncoming(
            MoneyTransaction transaction,
            Set<UUID> accountIds
    ) {
        boolean sourceOwned =
                transaction.getSourceAccountId() != null
                        && accountIds.contains(
                        transaction.getSourceAccountId()
                );

        boolean destinationOwned =
                transaction.getDestinationAccountId() != null
                        && accountIds.contains(
                        transaction.getDestinationAccountId()
                );

        if (sourceOwned && destinationOwned) {
            return null;
        }

        if (destinationOwned) {
            return true;
        }

        if (sourceOwned) {
            return false;
        }

        return null;
    }

    private Specification<MoneyTransaction> buildSpecification(
            List<UUID> accountIds,
            GetUserTransactionsQuery query
    ) {
        Specification<MoneyTransaction> specification =
                TransactionSpecifications.accounts(accountIds);

        if (query.status() != null) {
            specification = specification.and(
                    TransactionSpecifications.status(query.status())
            );
        }

        if (query.type() != null) {
            specification = specification.and(
                    TransactionSpecifications.type(query.type())
            );
        }

        if (query.currency() != null) {
            specification = specification.and(
                    TransactionSpecifications.currency(query.currency())
            );
        }

        if (query.from() != null) {
            specification = specification.and(
                    TransactionSpecifications.createdAtFrom(query.from())
            );
        }

        if (query.to() != null) {
            specification = specification.and(
                    TransactionSpecifications.createdAtTo(query.to())
            );
        }

        return specification;
    }

    private void validateQuery(
            GetUserTransactionsQuery query
    ) {
        if (query == null
                || query.userId() == null) {

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