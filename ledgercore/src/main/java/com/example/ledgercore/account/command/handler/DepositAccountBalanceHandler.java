package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DepositAccountCommand;
import com.example.ledgercore.account.command.port.inbound.DepositAccountBalanceUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositAccountBalanceHandler
        implements DepositAccountBalanceUseCase {

    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional
    public void execute(
            DepositAccountCommand command
    ) {
        Account account =
                accountCommandRepository
                        .findById(command.accountId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        validateAccount(account);

        account.setBalance(
                account.getBalance()
                        .add(command.amount())
        );
    }

    private void validateAccount(
            Account account
    ) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSACTION_STATUS
            );
        }
    }
}