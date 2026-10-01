package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.ledger.command.dto.RecordCreditInterestPostingCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditInterestPostingUseCase;
import com.example.ledgercore.transaction.command.port.outbound.CreditInterestLedgerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditInterestLedgerAdapter
        implements CreditInterestLedgerPort {

    private final RecordCreditInterestPostingUseCase useCase;

    @Override
    public void recordInterestPosting(
            UUID transactionId,
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        useCase.execute(
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}