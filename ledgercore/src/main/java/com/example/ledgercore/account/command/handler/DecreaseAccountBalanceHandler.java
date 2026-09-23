package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DecreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.port.inbound.DecreaseAccountBalanceUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.service.AccountDailyBalanceService;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DecreaseAccountBalanceHandler
        implements DecreaseAccountBalanceUseCase {

    private final AccountCommandRepository accountCommandRepository;
    private final AccountDailyBalanceService accountDailyBalanceService;

    @Override
    @Transactional
    public void execute(
            DecreaseAccountBalanceCommand command
    ) {
        validateCommand(command);

        Account account =
                accountCommandRepository
                        .findById(command.accountId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        validateAccount(account, command);

        if (account.getBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }

        BigDecimal newBalance =
                account.getBalance()
                        .subtract(command.amount());

        account.setBalance(newBalance);

        accountDailyBalanceService.updateClosingBalance(
                account.getId(),
                command.businessDate(),
                newBalance
        );
    }

    private void validateCommand(
            DecreaseAccountBalanceCommand command
    ) {
        if (command == null
                || command.accountId() == null
                || command.amount() == null
                || command.currency() == null
                || command.businessDate() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_AMOUNT
            );
        }
    }

    private void validateAccount(
            Account account,
            DecreaseAccountBalanceCommand command
    ) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSACTION_STATUS
            );
        }

        if (account.getCurrency() != command.currency()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }
    }
}