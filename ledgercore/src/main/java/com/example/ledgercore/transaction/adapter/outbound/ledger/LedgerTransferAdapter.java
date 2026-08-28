package com.example.ledgercore.transaction.adapter.outbound.ledger;

import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordTransferUseCase;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerTransferAdapter
        implements LedgerTransferPort {

    private final RecordTransferUseCase recordTransferUseCase;

    @Override
    public void recordTransfer(
            UUID transactionId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency
    ) {
        recordTransferUseCase.execute(
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency
                )
        );
    }
}