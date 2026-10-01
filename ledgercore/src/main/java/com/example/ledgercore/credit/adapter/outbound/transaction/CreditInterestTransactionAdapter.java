package com.example.ledgercore.credit.adapter.outbound.transaction;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.CreditInterestTransactionPort;
import com.example.ledgercore.transaction.command.dto.PostCreditInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.inbound.PostCreditInterestTransactionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditInterestTransactionAdapter
        implements CreditInterestTransactionPort {

    private final PostCreditInterestTransactionUseCase useCase;

    @Override
    public void postInterest(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        useCase.execute(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}