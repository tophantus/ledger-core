package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.mapper.AdminUserAccountMapper;
import com.example.ledgercore.account.query.dto.AdminUserAccountFilter;
import com.example.ledgercore.account.query.dto.AdminUserAccountResponse;
import com.example.ledgercore.account.query.port.inbound.GetAdminUserAccountsUseCase;
import com.example.ledgercore.account.query.projection.AdminUserAccountProjection;
import com.example.ledgercore.account.query.repository.UserAccountQueryRepository;
import com.example.ledgercore.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminUserAccountsHandler
        implements GetAdminUserAccountsUseCase {

    private final UserAccountQueryRepository userAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserAccountResponse> execute(
            AdminUserAccountFilter filter
    ) {
        validateFilter(filter);

        Pageable pageable =
                PageRequest.of(
                        filter.page(),
                        filter.size()
                );

        Page<AdminUserAccountProjection> accountPage =
                userAccountQueryRepository.findAdminUserAccounts(
                        filter.userId(),
                        normalize(filter.accountNo()),
                        filter.status() == null
                                ? null
                                : filter.status().name(),
                        filter.currency() == null
                                ? null
                                : filter.currency().name(),
                        pageable
                );

        return new PageResponse<>(
                accountPage.getContent()
                        .stream()
                        .map(AdminUserAccountMapper::toResponse)
                        .toList(),
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages()
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void validateFilter(
            AdminUserAccountFilter filter
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