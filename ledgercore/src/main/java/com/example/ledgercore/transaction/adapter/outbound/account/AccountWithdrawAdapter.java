package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;
import com.example.ledgercore.account.command.port.inbound.WithdrawAccountBalanceUseCase;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.port.inbound.GetWithdrawAccountInfoUseCase;
import com.example.ledgercore.account.query.port.inbound.CheckAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.AccountWithdrawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountWithdrawAdapter
        implements AccountWithdrawPort {

    private final GetWithdrawAccountInfoUseCase
            getWithdrawAccountInfoUseCase;

    private final CheckAccountOwnershipUseCase
            checkAccountOwnershipUseCase;

    private final WithdrawAccountBalanceUseCase
            withdrawAccountBalanceUseCase;

    @Override
    public AccountWithdrawPort.WithdrawAccountInfo getWithdrawInfo(
            UUID userId,
            UUID sourceAccountId
    ) {
        AccountWithdrawInfo info =
                getWithdrawAccountInfoUseCase.execute(
                        userId,
                        sourceAccountId
                );

        return new AccountWithdrawPort.WithdrawAccountInfo(
                info.accountId(),
                info.currency(),
                info.balance()
        );
    }

    @Override
    public void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    ) {
        boolean isOwner = checkAccountOwnershipUseCase.execute(
                userId,
                sourceAccountId
        );

        if (!isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    public void withdraw(
            UUID sourceAccountId,
            BigDecimal amount
    ) {
        withdrawAccountBalanceUseCase.execute(
                new WithdrawAccountCommand(
                        sourceAccountId,
                        amount
                )
        );
    }
}