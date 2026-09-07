package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AdminAccountFilter;
import com.example.ledgercore.account.query.dto.AdminAccountResponse;
import com.example.ledgercore.account.query.port.inbound.GetAdminAccountsUseCase;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminAccountsHandler
        implements GetAdminAccountsUseCase {

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAccountResponse> execute(
            AdminAccountFilter filter
    ) {
        validateFilter(filter);

        Pageable pageable =
                PageRequest.of(
                        filter.page(),
                        filter.size(),
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Specification<Account> specification =
                buildSpecification(filter);

        Page<Account> accountPage =
                accountQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                accountPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages()
        );
    }

    private Specification<Account> buildSpecification(
            AdminAccountFilter filter
    ) {
        Specification<Account> specification =
                (root, query, cb) -> cb.conjunction();

        if (filter.accountNo() != null
                && !filter.accountNo().isBlank()) {

            String accountNo =
                    "%" + filter.accountNo().trim() + "%";

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    root.get("accountNo"),
                                    accountNo
                            )
            );
        }

        if (filter.status() != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    filter.status()
                            )
            );
        }

        if (filter.currency() != null
                && !filter.currency().isBlank()) {

            String currency =
                    filter.currency().trim().toUpperCase();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("currency"),
                                    currency
                            )
            );
        }

        if (filter.userId() != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("userId"),
                                    filter.userId()
                            )
            );
        }

        return specification;
    }

    private AdminAccountResponse toResponse(
            Account account
    ) {
        return new AdminAccountResponse(
                account.getId(),
                account.getUserId(),
                account.getAccountNo(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getLedgerAccountId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private void validateFilter(
            AdminAccountFilter filter
    ) {
        if (filter == null) {
            throw new IllegalArgumentException(
                    "filter must not be null"
            );
        }

        if (filter.page() < 0) {
            throw new IllegalArgumentException(
                    "page must not be negative"
            );
        }

        if (filter.size() <= 0 || filter.size() > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100"
            );
        }
    }
}