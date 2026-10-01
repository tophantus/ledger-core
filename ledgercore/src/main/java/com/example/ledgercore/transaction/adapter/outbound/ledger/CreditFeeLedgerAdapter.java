package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.ledger.command.dto.RecordCreditFeeCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditFeeUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditFeeLedgerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditFeeLedgerAdapter
        implements CreditFeeLedgerPort {

    private final RecordCreditFeeUseCase useCase;

    @Override
    public void recordCreditFee(
            UUID transactionId,
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        useCase.execute(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}