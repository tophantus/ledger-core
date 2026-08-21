package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.port.inbound.GetTransferAccountInfoUseCase;
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

    private final AccountQueryRepository accountQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountTransferInfo execute(
            UUID userId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        Account sourceAccount = accountQueryRepository
                .findByIdAndUserId(sourceAccountId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        Account destinationAccount = accountQueryRepository
                .findById(destinationAccountId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        if (sourceAccount.getStatus()
                != AccountStatus.ACTIVE
                || destinationAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new AccountTransferInfo(
                sourceAccount.getId(),
                destinationAccount.getId(),
                sourceAccount.getCurrency(),
                sourceAccount.getBalance()
        );
    }
}