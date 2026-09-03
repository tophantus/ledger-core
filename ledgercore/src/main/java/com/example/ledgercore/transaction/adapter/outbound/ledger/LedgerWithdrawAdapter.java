package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordWithdrawUseCase;
import com.example.ledgercore.transaction.command.port.outbound.LedgerWithdrawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerWithdrawAdapter
        implements LedgerWithdrawPort {

    private final RecordWithdrawUseCase
            recordWithdrawUseCase;

    @Override
    public void recordWithdraw(
            UUID transactionId,
            UUID sourceAccountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    ) {
        recordWithdrawUseCase.execute(
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }
}