package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.account.command.dto.IncreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.port.inbound.IncreaseAccountBalanceUseCase;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.dto
        .DecreaseCreditFacilityOutstandingBalanceCommand;
import com.example.ledgercore.credit.command.port.inbound
        .DecreaseCreditFacilityOutstandingBalanceUseCase;
import com.example.ledgercore.transaction.command.port.outbound
        .TransferCreditFacilityToProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferCreditFacilityToProviderAdapter
        implements TransferCreditFacilityToProviderPort {

    private final DecreaseCreditFacilityOutstandingBalanceUseCase
            decreaseCreditFacilityOutstandingBalanceUseCase;

    private final IncreaseAccountBalanceUseCase
            increaseAccountBalanceUseCase;

    @Override
    public void decreaseCreditFacilityOutstandingBalance(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        decreaseCreditFacilityOutstandingBalanceUseCase.execute(
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        creditFacilityId,
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