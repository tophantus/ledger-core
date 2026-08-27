package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.query.dto.AccountHolderResponse;
import com.example.ledgercore.account.query.dto.GetAccountHolderQuery;
import com.example.ledgercore.account.query.port.inbound.GetAccountHolderUseCase;
import com.example.ledgercore.account.query.port.outbound.AccountHolderProfilePort;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountHolderHandler
        implements GetAccountHolderUseCase {

    private final AccountQueryRepository accountQueryRepository;
    private final AccountHolderProfilePort accountHolderProfilePort;

    @Override
    @Transactional(readOnly = true)
    public AccountHolderResponse execute(
            GetAccountHolderQuery query
    ) {
        validateQuery(query);

        Account account =
                accountQueryRepository
                        .findByAccountNo(query.accountNo())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        String fullName =
                accountHolderProfilePort.getFullName(
                        account.getUserId()
                );

        return new AccountHolderResponse(
                account.getAccountNo(),
                fullName
        );
    }

    private void validateQuery(
            GetAccountHolderQuery query
    ) {
        if (query == null
                || query.accountNo() == null
                || query.accountNo().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}