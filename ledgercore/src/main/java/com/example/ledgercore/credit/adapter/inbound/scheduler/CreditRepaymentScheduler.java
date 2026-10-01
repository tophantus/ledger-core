package com.example.ledgercore.credit.adapter.inbound.scheduler;

import com.example.ledgercore.credit.command.port.inbound.EnqueueDueRepaymentsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "credit.repayment.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class CreditRepaymentScheduler {

    private final EnqueueDueRepaymentsUseCase enqueueDueRepaymentsUseCase;

    @Scheduled(
            fixedDelayString =
                    "${credit.repayment.scheduler.fixed-delay:300000}"
    )
    public void process() {
        enqueueDueRepaymentsUseCase.execute();
    }
}