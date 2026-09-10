package com.example.ledgercore.withdrawal.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.entity.WithdrawalLookupCode;
import com.example.ledgercore.withdrawal.query.dto.GetWithdrawalIntentIdByLookupCodeQuery;
import com.example.ledgercore.withdrawal.query.port.inbound.GetWithdrawalIntentIdByLookupCodeUseCase;
import com.example.ledgercore.withdrawal.query.repository.WithdrawalLookupCodeQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWithdrawalIntentIdByLookupCodeHandler
        implements GetWithdrawalIntentIdByLookupCodeUseCase {

    private final WithdrawalLookupCodeQueryRepository
            withdrawalLookupCodeQueryRepository;

    @Override
    public UUID execute(
            GetWithdrawalIntentIdByLookupCodeQuery query
    ) {
        validateQuery(query);

        WithdrawalLookupCode lookupCode =
                withdrawalLookupCodeQueryRepository
                        .findByLookupCode(
                                query.lookupCode().trim()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .WITHDRAWAL_LOOKUP_CODE_NOT_FOUND
                                )
                        );

        return lookupCode.getWithdrawalIntentId();
    }

    private void validateQuery(
            GetWithdrawalIntentIdByLookupCodeQuery query
    ) {
        if (query == null
                || query.lookupCode() == null
                || query.lookupCode().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}