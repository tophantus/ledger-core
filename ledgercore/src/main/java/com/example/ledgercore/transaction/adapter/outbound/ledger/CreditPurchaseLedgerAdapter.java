package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.ledger.command.dto.RecordCreditPurchaseCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditPurchaseUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditPurchaseLedgerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditPurchaseLedgerAdapter
        implements CreditPurchaseLedgerPort {

    private final RecordCreditPurchaseUseCase
            recordCreditPurchaseUseCase;

    @Override
    public void recordCreditPayment(
            UUID transactionId,
            UUID creditFacilityId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        recordCreditPurchaseUseCase.execute(
                new RecordCreditPurchaseCommand(
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