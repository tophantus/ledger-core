package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.event.OutboxAggregateType;
import com.example.ledgercore.outbox.event.OutboxEventType;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventAdapter implements TransactionEventPort {

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;

    @Override
    public void publishTransferCompleted(
            TransferCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.TRANSACTION.getValue(),
                event.transactionId(),
                OutboxEventType.TRANSFER_COMPLETED.getValue(),
                event
        );
    }

    @Override
    public void publishDepositCompleted(
            DepositCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.TRANSACTION.getValue(),
                event.transactionId(),
                OutboxEventType.DEPOSIT_COMPLETED.getValue(),
                event
        );
    }

    @Override
    public void publishWithdrawCompleted(
            WithdrawCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.TRANSACTION.getValue(),
                event.transactionId(),
                OutboxEventType.WITHDRAW_COMPLETED.getValue(),
                event
        );
    }
}