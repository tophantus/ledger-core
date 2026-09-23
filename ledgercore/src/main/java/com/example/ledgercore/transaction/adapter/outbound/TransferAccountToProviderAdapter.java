package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.account.command.dto.DecreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.dto.IncreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.port.inbound.DecreaseAccountBalanceUseCase;
import com.example.ledgercore.account.command.port.inbound.IncreaseAccountBalanceUseCase;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.command.port.outbound.TransferAccountToProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferAccountToProviderAdapter
        implements TransferAccountToProviderPort {

    private final DecreaseAccountBalanceUseCase
            decreaseAccountBalanceUseCase;

    private final IncreaseAccountBalanceUseCase
            increaseAccountBalanceUseCase;

    @Override
    public void decreaseSourceAccount(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        decreaseAccountBalanceUseCase.execute(
                new DecreaseAccountBalanceCommand(
                        accountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }

    @Override
    public void increaseProviderAccount(
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        increaseAccountBalanceUseCase.execute(
                new IncreaseAccountBalanceCommand(
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}