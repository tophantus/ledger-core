package com.example.ledgercore.transaction.adapter.outbound.account;

import com.example.ledgercore.account.command.dto.DecreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.dto.IncreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.port.inbound.DecreaseAccountBalanceUseCase;
import com.example.ledgercore.account.command.port.inbound.IncreaseAccountBalanceUseCase;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.transaction.command.port.outbound.RepayCreditFacilityAccountBalancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepayCreditFacilityRepayCreditFacilityAccountBalanceAdapter
        implements RepayCreditFacilityAccountBalancePort {

    private final IncreaseAccountBalanceUseCase
            increaseAccountBalanceUseCase;

    private final DecreaseAccountBalanceUseCase
            decreaseAccountBalanceUseCase;

    @Override
    public void increaseBalance(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        increaseAccountBalanceUseCase.execute(
                new IncreaseAccountBalanceCommand(
                        accountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }

    @Override
    public void decreaseBalance(
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
}