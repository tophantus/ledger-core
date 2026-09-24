package com.example.ledgercore.transfer.adapter.outbound.transaction;

import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transfer.command.port.outbound.TransferTransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionTransferAdapter
        implements TransferTransactionPort {

    private final TransferMoneyUseCase transferMoneyUseCase;

    @Override
    public TransactionResponse transfer(
            TransferMoneyCommand command
    ) {
        return transferMoneyUseCase.execute(command);
    }
}
