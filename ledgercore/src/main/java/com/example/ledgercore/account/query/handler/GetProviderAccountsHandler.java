package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.query.dto.AccountSummaryResponse;
import com.example.ledgercore.account.query.dto.GetProviderAccountsQuery;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountsUseCase;
import com.example.ledgercore.account.query.port.outbound.AccountAuthenticateProviderPort;
import com.example.ledgercore.account.query.repository.ProviderAccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProviderAccountsHandler
        implements GetProviderAccountsUseCase {

    private final AccountAuthenticateProviderPort
            accountAuthenticateProviderPort;

    private final ProviderAccountQueryRepository
            providerAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> execute(
            GetProviderAccountsQuery query
    ) {
        validate(query);

        UUID providerId =
                accountAuthenticateProviderPort.authenticate(
                        query.clientId(),
                        query.credential()
                );

        return providerAccountQueryRepository
                .findAllByProviderIdAndStatusNot(
                        providerId,
                        AccountStatus.CLOSED
                )
                .stream()
                .map(AccountMapper::toSummaryResponse)
                .toList();
    }

    private void validate(
            GetProviderAccountsQuery query
    ) {
        if (query == null
                || query.clientId() == null
                || query.clientId().isBlank()
                || query.credential() == null
                || query.credential().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}