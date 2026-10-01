package com.example.ledgercore.credit.adapter.outbound.transaction;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.CreditFacilityFeePort;
import com.example.ledgercore.transaction.command.dto.CreateCreditFacilityFeeCommand;
import com.example.ledgercore.transaction.command.port.inbound.CreateCreditFacilityFeeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditFacilityFeeAdapter
        implements CreditFacilityFeePort {

    private final CreateCreditFacilityFeeUseCase useCase;

    @Override
    public void createFee(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    ) {
        useCase.execute(
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        amount,
                        currency,
                        reference,
                        description
                )
        );
    }
}