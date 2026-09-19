package com.example.ledgercore.credit.adapter.inbound.rabbit;

import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.credit.command.port.inbound.CreateCreditOfferRunUseCase;
import com.example.ledgercore.credit.config.CreditOfferRunRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditOfferRunBusinessDayClosedConsumer {

    private final CreateCreditOfferRunUseCase
            createCreditOfferRunUseCase;

    @RabbitListener(
            queues = CreditOfferRunRabbitConfig.CREDIT_OFFER_RUN_QUEUE
    )
    public void consume(BusinessDayClosedEvent event) {

        createCreditOfferRunUseCase.execute(
                event.businessDate()
        );
    }
}