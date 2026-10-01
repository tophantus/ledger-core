package com.example.ledgercore.credit.adapter.inbound.rabbit;

import com.example.ledgercore.credit.command.port.inbound.RepayCreditFacilityUseCase;
import com.example.ledgercore.credit.config.CreditRepaymentRabbitConfig;
import com.example.ledgercore.credit.messaging.repayment.RepaymentJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepaymentJobRabbitConsumer {

    private final RepayCreditFacilityUseCase repayCreditFacilityUseCase;

    @RabbitListener(
            queues = CreditRepaymentRabbitConfig.CREDIT_REPAYMENT_QUEUE
    )
    public void consume(RepaymentJob job) {
        log.debug(
                "Received credit repayment job for statement {}",
                job.statementId()
        );

        repayCreditFacilityUseCase.execute(job.statementId());
    }
}