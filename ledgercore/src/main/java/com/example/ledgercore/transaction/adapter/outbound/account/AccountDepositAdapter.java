package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.DepositAccountCommand;
import com.example.ledgercore.account.command.port.inbound.DepositAccountBalanceUseCase;
import com.example.ledgercore.account.query.dto.AccountDepositInfo;
import com.example.ledgercore.account.query.port.inbound.GetDepositAccountInfoUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountDepositPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDepositAdapter implements AccountDepositPort {

    private final GetDepositAccountInfoUseCase
            getDepositAccountInfoUseCase;

    private final DepositAccountBalanceUseCase
            depositAccountBalanceUseCase;

    @Override
    public AccountDepositPort.DepositAccountInfo getDepositInfo(
            UUID destinationAccountId
    ) {
        AccountDepositInfo info =
                getDepositAccountInfoUseCase.execute(
                        destinationAccountId
                );

        return new AccountDepositPort.DepositAccountInfo(
                info.accountId(),
                info.currency()
        );
    }

    @Override
    public void deposit(
            UUID destinationAccountId,
            BigDecimal amount,
            LocalDate businessDate
    ) {
        depositAccountBalanceUseCase.execute(
                new DepositAccountCommand(
                        destinationAccountId,
                        amount,
                        businessDate
                )
        );
    }
}