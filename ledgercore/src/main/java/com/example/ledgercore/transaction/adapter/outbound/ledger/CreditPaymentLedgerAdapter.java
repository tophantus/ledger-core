package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.ledger.command.dto.RecordCreditPaymentCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditPaymentUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditPaymentLedgerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditPaymentLedgerAdapter
        implements CreditPaymentLedgerPort {

    private final RecordCreditPaymentUseCase
            recordCreditPaymentUseCase;

    @Override
    public void recordCreditPayment(
            UUID transactionId,
            UUID creditFacilityId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        recordCreditPaymentUseCase.execute(
                new RecordCreditPaymentCommand(
                        transactionId,
                        creditFacilityId,
                        providerAccountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}