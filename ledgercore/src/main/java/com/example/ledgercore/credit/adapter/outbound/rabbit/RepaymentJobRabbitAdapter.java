package com.example.ledgercore.credit.adapter.outbound.rabbit;

import com.example.ledgercore.credit.config.CreditRepaymentRabbitConfig;
import com.example.ledgercore.credit.messaging.repayment.RepaymentJob;
import com.example.ledgercore.credit.command.port.outbound.RepaymentJobPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepaymentJobRabbitAdapter
        implements RepaymentJobPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(RepaymentJob job) {
        rabbitTemplate.convertAndSend(
                CreditRepaymentRabbitConfig.CREDIT_REPAYMENT_EXCHANGE,
                CreditRepaymentRabbitConfig.CREDIT_REPAYMENT_ROUTING_KEY,
                job
        );
    }
}