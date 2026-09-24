package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetProviderAccountByCredentialQuery;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountByCredentialUseCase;
import com.example.ledgercore.account.query.port.outbound.AccountAuthenticateProviderPort;
import com.example.ledgercore.account.query.projection.ProviderAccountProjection;
import com.example.ledgercore.account.query.repository.ProviderAccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProviderAccountByCredentialHandler
        implements GetProviderAccountByCredentialUseCase {

    private final AccountAuthenticateProviderPort
            accountAuthenticateProviderPort;

    private final ProviderAccountQueryRepository
            providerAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse execute(
            GetProviderAccountByCredentialQuery query
    ) {
        validate(query);

        UUID providerId =
                accountAuthenticateProviderPort.authenticate(
                        query.clientId(),
                        query.credential()
                );

        ProviderAccountProjection projection =
                providerAccountQueryRepository
                        .findByProviderIdAndAccountId(
                                providerId,
                                query.accountId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        return AccountMapper.toResponse(projection);
    }

    private void validate(
            GetProviderAccountByCredentialQuery query
    ) {
        if (query == null
                || query.clientId() == null
                || query.clientId().isBlank()
                || query.credential() == null
                || query.credential().isBlank()
                || query.accountId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}