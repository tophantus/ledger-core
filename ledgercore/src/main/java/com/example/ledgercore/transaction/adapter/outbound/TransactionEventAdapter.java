package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventAdapter implements TransactionEventPort {

    private static final String AGGREGATE_TYPE = "TRANSACTION";

    private static final String TRANSFER_COMPLETED =
            "TRANSFER_COMPLETED";

    private static final String DEPOSIT_COMPLETED =
            "DEPOSIT_COMPLETED";

    private static final String WITHDRAW_COMPLETED =
            "WITHDRAW_COMPLETED";

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;

    @Override
    public void publishTransferCompleted(
            TransferCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                AGGREGATE_TYPE,
                event.transactionId(),
                TRANSFER_COMPLETED,
                event
        );
    }

    @Override
    public void publishDepositCompleted(
            DepositCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                AGGREGATE_TYPE,
                event.transactionId(),
                DEPOSIT_COMPLETED,
                event
        );
    }

    @Override
    public void publishWithdrawCompleted(
            WithdrawCompletedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                AGGREGATE_TYPE,
                event.transactionId(),
                WITHDRAW_COMPLETED,
                event
        );
    }
}