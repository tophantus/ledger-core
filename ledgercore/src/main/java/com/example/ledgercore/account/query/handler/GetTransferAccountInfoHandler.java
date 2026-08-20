package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.TransferAccountInfo;
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
    public TransferAccountInfo execute(
            UUID userId,
            UUID sourceAccountId,
            String destinationAccountNo
    ) {
        Account sourceAccount = accountQueryRepository
                .findByIdAndUserId(sourceAccountId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        Account destinationAccount = accountQueryRepository
                .findByAccountNo(destinationAccountNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        if (sourceAccount.getStatus()
                != AccountStatus.ACTIVE
                || destinationAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        return new TransferAccountInfo(
                sourceAccount.getId(),
                destinationAccount.getId(),
                sourceAccount.getCurrency(),
                sourceAccount.getBalance()
        );
    }
}