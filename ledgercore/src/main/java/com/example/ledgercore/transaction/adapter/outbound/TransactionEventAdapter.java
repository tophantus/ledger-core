package com.example.ledgercore.transaction.adapter.outbound;

import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.event.OutboxAggregateType;
import com.example.ledgercore.outbox.event.OutboxEventType;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventAdapter implements TransactionEventPort {

    private final SaveOutboxEventUseCase saveOutboxEventUseCase;

    @Override
    public void publishAccountBalanceChanged(
            AccountBalanceChangedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.TRANSACTION.getValue(),
                event.transactionId(),
                OutboxEventType.ACCOUNT_BALANCE_CHANGED.getValue(),
                event
        );
    }

    @Override
    public void publishCreditFacilityBalanceChanged(
            CreditFacilityBalanceChangedEvent event
    ) {
        saveOutboxEventUseCase.execute(
                OutboxAggregateType.TRANSACTION.getValue(),
                event.transactionId(),
                OutboxEventType.CREDIT_FACILITY_BALANCE_CHANGED.getValue(),
                event
        );
    }
}