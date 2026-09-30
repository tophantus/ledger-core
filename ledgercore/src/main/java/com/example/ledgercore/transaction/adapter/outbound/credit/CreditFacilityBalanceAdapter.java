package com.example.ledgercore.transaction.adapter.outbound.credit;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.dto.DecreaseCreditFacilityOutstandingBalanceCommand;
import com.example.ledgercore.credit.command.port.inbound.DecreaseCreditFacilityOutstandingBalanceUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditFacilityBalanceAdapter
        implements CreditFacilityBalancePort {

    private final DecreaseCreditFacilityOutstandingBalanceUseCase
            decreaseCreditFacilityOutstandingBalanceUseCase;

    @Override
    public void decreaseOutstandingBalance(
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
}