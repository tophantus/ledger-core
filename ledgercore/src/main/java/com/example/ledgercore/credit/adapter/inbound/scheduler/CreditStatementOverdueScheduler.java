package com.example.ledgercore.credit.adapter.inbound.scheduler;

import com.example.ledgercore.credit.command.port.inbound.ProcessOverdueCreditStatementsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "credit.statement.overdue.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class CreditStatementOverdueScheduler {

    private final ProcessOverdueCreditStatementsUseCase
            processOverdueCreditStatementsUseCase;

    @Scheduled(
            fixedDelayString =
                    "${credit.statement.overdue.scheduler.fixed-delay:300000}"
    )
    public void process() {
        processOverdueCreditStatementsUseCase.execute();
    }
}