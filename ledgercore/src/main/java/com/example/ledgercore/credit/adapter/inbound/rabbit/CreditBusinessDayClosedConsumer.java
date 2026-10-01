package com.example.ledgercore.credit.adapter.inbound.rabbit;

import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditOfferRunUseCase;
import com.example.ledgercore.credit.config.CreditBusinessDayRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditBusinessDayClosedConsumer {

    private final CreateCreditOfferRunUseCase
            createCreditOfferRunUseCase;

    @RabbitListener(
            queues = CreditBusinessDayRabbitConfig.CREDIT_BUSINESS_DAY_CLOSED_QUEUE
    )
    public void consume(BusinessDayClosedEvent event) {
        createCreditOfferRunUseCase.execute(
                event.businessDate()
        );
    }
}