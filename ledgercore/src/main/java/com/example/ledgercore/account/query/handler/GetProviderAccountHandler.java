package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.mapper.AccountMapper;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.port.inbound.GetProviderAccountUseCase;
import com.example.ledgercore.account.query.projection.ProviderAccountProjection;
import com.example.ledgercore.account.query.repository.ProviderAccountQueryRepository;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProviderAccountHandler
        implements GetProviderAccountUseCase {

    private final ProviderAccountQueryRepository
            providerAccountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse execute(
            UUID providerId,
            Currency currency
    ) {
        validate(providerId, currency);

        ProviderAccountProjection projection =
                providerAccountQueryRepository
                        .findByProviderIdAndCurrency(
                                providerId,
                                currency
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        return AccountMapper.toResponse(projection);
    }

    private void validate(
            UUID providerId,
            Currency currency
    ) {
        if (providerId == null || currency == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}