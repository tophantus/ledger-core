package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;
import com.example.ledgercore.account.command.port.inbound.WithdrawAccountBalanceUseCase;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.service.AccountDailyBalanceService;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawAccountBalanceHandler
        implements WithdrawAccountBalanceUseCase {

    private final AccountCommandRepository accountCommandRepository;

    private final AccountDailyBalanceService accountDailyBalanceService;

    @Override
    @Transactional
    public void execute(
            WithdrawAccountCommand command
    ) {
        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }

        Account account =
                accountCommandRepository
                        .findById(command.accountId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_NOT_FOUND
                                )
                        );

        if (account.getStatus()
                != com.example.ledgercore.account.enums.AccountStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }

        if (account.getBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }

        account.setBalance(
                account.getBalance()
                        .subtract(command.amount())
        );

        accountDailyBalanceService.updateClosingBalance(
                account.getId(),
                command.businessDate(),
                account.getBalance()
        );

        accountCommandRepository.save(account);
    }
}