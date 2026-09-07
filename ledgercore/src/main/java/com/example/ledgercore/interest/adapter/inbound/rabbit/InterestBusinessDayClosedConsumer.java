package com.example.ledgercore.interest.adapter.inbound.rabbit;

import com.example.ledgercore.businessday.event.BusinessDayClosedEvent;
import com.example.ledgercore.interest.command.port.inbound.CreateInterestAccrualRunUseCase;
import com.example.ledgercore.interest.config.InterestRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterestBusinessDayClosedConsumer {

    private final CreateInterestAccrualRunUseCase
            createInterestAccrualRunUseCase;

    @RabbitListener(
            queues = InterestRabbitConfig.INTEREST_QUEUE
    )
    public void consume(BusinessDayClosedEvent event) {

        createInterestAccrualRunUseCase.execute(
                event.businessDate()
        );
    }
}