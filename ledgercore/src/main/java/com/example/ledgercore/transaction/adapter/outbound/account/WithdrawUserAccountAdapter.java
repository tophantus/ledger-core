package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;
import com.example.ledgercore.account.command.port.inbound.WithdrawAccountBalanceUseCase;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.port.inbound.GetWithdrawAccountInfoUseCase;
import com.example.ledgercore.account.query.port.inbound.CheckUserAccountOwnershipUseCase;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.WithdrawUserAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WithdrawUserAccountAdapter
        implements WithdrawUserAccountPort {

    private final GetWithdrawAccountInfoUseCase
            getWithdrawAccountInfoUseCase;

    private final CheckUserAccountOwnershipUseCase
            checkUserAccountOwnershipUseCase;

    private final WithdrawAccountBalanceUseCase
            withdrawAccountBalanceUseCase;

    @Override
    public WithdrawUserAccountPort.WithdrawAccountInfo getWithdrawInfo(
            UUID userId,
            UUID sourceAccountId
    ) {
        AccountWithdrawInfo info =
                getWithdrawAccountInfoUseCase.execute(
                        sourceAccountId
                );

        return new WithdrawUserAccountPort.WithdrawAccountInfo(
                info.accountId(),
                info.userId(),
                info.currency(),
                info.availableBalance()
        );
    }

    @Override
    public void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    ) {
        boolean isOwner = checkUserAccountOwnershipUseCase.execute(
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
            BigDecimal amount,
            LocalDate businessDate
    ) {
        withdrawAccountBalanceUseCase.execute(
                new WithdrawAccountCommand(
                        sourceAccountId,
                        amount,
                        businessDate
                )
        );
    }
}