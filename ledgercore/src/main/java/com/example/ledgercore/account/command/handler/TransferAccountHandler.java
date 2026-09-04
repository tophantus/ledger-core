package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
import com.example.ledgercore.account.command.port.inbound.TransferAccountBalanceUseCase;
import com.example.ledgercore.account.command.service.AccountDailyBalanceService;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferAccountHandler
        implements TransferAccountBalanceUseCase {

    private final AccountCommandRepository accountCommandRepository;

    private final AccountDailyBalanceService accountDailyBalanceService;

    @Override
    @Transactional
    public void execute(
            TransferAccountCommand command
    ) {
        validateAmount(command.amount());

        Account sourceAccount = accountCommandRepository
                .findById(command.sourceAccountId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        Account destinationAccount = accountCommandRepository
                .findById(command.destinationAccountId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                ));

        validateAccounts(
                sourceAccount,
                destinationAccount
        );

        validateCurrency(
                sourceAccount,
                destinationAccount
        );

        validateBalance(
                sourceAccount,
                command.amount()
        );

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(command.amount())
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(command.amount())
        );

        accountDailyBalanceService.updateClosingBalance(
                sourceAccount.getId(),
                command.businessDate(),
                sourceAccount.getBalance()
        );

        accountDailyBalanceService.updateClosingBalance(
                destinationAccount.getId(),
                command.businessDate(),
                destinationAccount.getBalance()
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }
    }

    private void validateAccounts(
            Account sourceAccount,
            Account destinationAccount
    ) {
        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE
                || destinationAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }
    }

    private void validateCurrency(
            Account sourceAccount,
            Account destinationAccount
    ) {
        if (!sourceAccount.getCurrency()
                .equals(destinationAccount.getCurrency())) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }
    }

    private void validateBalance(
            Account sourceAccount,
            BigDecimal amount
    ) {
        if (sourceAccount.getBalance()
                .compareTo(amount) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }
}