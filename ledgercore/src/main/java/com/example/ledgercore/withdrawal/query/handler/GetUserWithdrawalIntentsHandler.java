package com.example.ledgercore.withdrawal.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.query.dto.GetUserWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;
import com.example.ledgercore.withdrawal.query.port.inbound.GetUserWithdrawalIntentsUseCase;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountIdsPort;
import com.example.ledgercore.withdrawal.query.repository.WithdrawalIntentQueryRepository;
import com.example.ledgercore.withdrawal.query.specification.WithdrawalIntentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserWithdrawalIntentsHandler
        implements GetUserWithdrawalIntentsUseCase {

    private final WithdrawalIntentQueryRepository
            withdrawalIntentQueryRepository;

    private final AccountIdsPort accountIdsPort;

    @Override
    public PageResponse<WithdrawalIntentResponse> execute(
            GetUserWithdrawalIntentsQuery query
    ) {
        List<UUID> accountIds =
                accountIdsPort.getAccountIdsByUser(
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

        Specification<WithdrawalIntent> specification =
                WithdrawalIntentSpecification.accountIds(
                        accountIds
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