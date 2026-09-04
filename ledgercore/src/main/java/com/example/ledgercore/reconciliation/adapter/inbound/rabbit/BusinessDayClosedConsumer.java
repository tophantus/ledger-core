package com.example.ledgercore.reconciliation.adapter.inbound.rabbit;

import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.reconciliation.command.port.inbound.StartBusinessDayReconciliationUseCase;
import com.example.ledgercore.reconciliation.config.ReconciliationRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessDayClosedConsumer {

    private final StartBusinessDayReconciliationUseCase
            startBusinessDayReconciliationUseCase;

    @RabbitListener(
            queues = ReconciliationRabbitConfig.RECONCILIATION_QUEUE
    )
    public void consume(BusinessDayClosedEvent event) {

        startBusinessDayReconciliationUseCase.execute(
                event.businessDate()
        );
    }
}