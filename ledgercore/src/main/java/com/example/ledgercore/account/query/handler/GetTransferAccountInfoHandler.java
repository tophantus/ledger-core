package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.account.query.port.inbound.GetTransferAccountInfoUseCase;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTransferAccountInfoHandler
        implements GetTransferAccountInfoUseCase {

    private final GetUserAccountService getUserAccountService;
    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountTransferInfo execute(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {

        GetUserAccountResult sourceAccount = getUserAccountService.execute(
                new GetUserAccountQuery(sourceAccountId)
        );

        if (!sourceAccount.userId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Account destinationAccount = accountQueryRepository
                .findById(destinationAccountId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        if (sourceAccount.status()
                != AccountStatus.ACTIVE
                || destinationAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new AccountTransferInfo(
                sourceAccount.accountId(),
                destinationAccount.getId(),
                sourceAccount.currency(),
                sourceAccount.getAvailableBalance()
        );
    }
}