package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.ledger.command.dto.RecordDepositCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordDepositUseCase;
import com.example.ledgercore.transaction.command.port.outbound.LedgerDepositPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerDepositAdapter implements LedgerDepositPort {

    private final RecordDepositUseCase recordDepositUseCase;

    @Override
    public void recordDeposit(
            UUID transactionId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency
    ) {
        recordDepositUseCase.execute(
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
                        amount,
                        currency
                )
        );
    }
}