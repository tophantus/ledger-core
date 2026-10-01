package com.example.ledgercore.interest.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.port.outbound.CreditInterestJournalPort;
import com.example.ledgercore.ledger.command.dto.RecordCreditInterestAccrualJournalCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditInterestAccrualJournalUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditInterestJournalAdapter
        implements CreditInterestJournalPort {

    private final RecordCreditInterestAccrualJournalUseCase useCase;

    @Override
    public UUID recordAccrualJournal(
            UUID accrualId,
            LocalDate businessDate,
            Currency currency,
            BigDecimal amount
    ) {
        return useCase.execute(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        amount
                )
        );
    }
}