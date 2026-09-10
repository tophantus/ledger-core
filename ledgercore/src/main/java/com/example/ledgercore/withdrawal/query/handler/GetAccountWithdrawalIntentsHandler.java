package com.example.ledgercore.withdrawal.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.query.dto.GetAccountWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;
import com.example.ledgercore.withdrawal.query.port.inbound.GetAccountWithdrawalIntentsUseCase;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountOwnershipPort;
import com.example.ledgercore.withdrawal.query.repository.WithdrawalIntentQueryRepository;
import com.example.ledgercore.withdrawal.query.specification.WithdrawalIntentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAccountWithdrawalIntentsHandler
        implements GetAccountWithdrawalIntentsUseCase {

    private final WithdrawalIntentQueryRepository
            withdrawalIntentQueryRepository;

    private final AccountOwnershipPort
            accountOwnershipPort;

    @Override
    public PageResponse<WithdrawalIntentResponse> execute(
            GetAccountWithdrawalIntentsQuery query
    ) {
        if (!accountOwnershipPort.checkOwnership(
                query.userId(),
                query.accountId()
        )) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        Specification<WithdrawalIntent> specification =
                WithdrawalIntentSpecification.accountId(
                        query.accountId()
                );

        if (query.status() != null) {
            specification = specification.and(
                    WithdrawalIntentSpecification.status(
                            query.status()
                    )
            );
        }

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<WithdrawalIntent> page =
                withdrawalIntentQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private WithdrawalIntentResponse toResponse(
            WithdrawalIntent intent
    ) {
        return new WithdrawalIntentResponse(
                intent.getId(),
                intent.getAccountId(),
                intent.getWithdrawalReference(),
                intent.getAmount().toPlainString(),
                intent.getCurrency(),
                intent.getStatus(),
                intent.getExpiresAt(),
                intent.getCreatedAt(),
                intent.getCompletedAt()
        );
    }
}