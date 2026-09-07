package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.ledger.command.dto.RecordInterestPostingCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordInterestPostingUseCase;
import com.example.ledgercore.transaction.command.port.outbound.InterestLedgerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InterestLedgerAdapter
        implements InterestLedgerPort {

    private final RecordInterestPostingUseCase useCase;

    @Override
    public void recordInterestPosting(
            UUID transactionId,
            UUID accountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    ) {
        useCase.execute(
                new RecordInterestPostingCommand(
                        transactionId,
                        accountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}