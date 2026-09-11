package com.example.ledgercore.interest.adapter.outbound.ledger;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.ledger.command.dto.RecordInterestAccrualJournalCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordInterestAccrualJournalUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InterestJournalAdapter
        implements InterestJournalPort {

    private final RecordInterestAccrualJournalUseCase useCase;

    @Override
    public UUID recordAccrualJournal(
            UUID accrualId,
            LocalDate businessDate,
            Currency currency,
            BigDecimal amount
    ) {
        return useCase.execute(
                new RecordInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        amount
                )
        );
    }
}