package com.example.ledgercore.hold.adapter.outbound.credit;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.inbound.IncreaseCreditFacilityHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.CreditHoldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditHoldAdapter
        implements CreditHoldPort {

    private final IncreaseCreditFacilityHoldUseCase
            increaseCreditFacilityHoldUseCase;

    @Override
    public void increaseHold(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency
    ) {
        increaseCreditFacilityHoldUseCase.execute(
                creditFacilityId,
                amount,
                currency
        );
    }
}