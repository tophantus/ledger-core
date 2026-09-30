package com.example.ledgercore.credit.adapter.inbound.scheduler;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditStatementRun;
import com.example.ledgercore.credit.command.port.inbound.run.ClaimCreditStatementRunUseCase;
import com.example.ledgercore.credit.command.port.inbound.ProcessCreditStatementRunUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "credit.statement.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class CreditStatementRunScheduler {

    private final ClaimCreditStatementRunUseCase claimCreditStatementRunUseCase;
    private final ProcessCreditStatementRunUseCase processCreditStatementRunUsecase;

    @Scheduled(
            fixedDelayString =
                    "${credit.statement.scheduler.fixed-delay:1000}"
    )
    public void process() {

        Optional<ClaimedCreditStatementRun> optionalRun =
                claimCreditStatementRunUseCase.execute(
                        Instant.now()
                );

        if (optionalRun.isEmpty()) {
            return;
        }

        processCreditStatementRunUsecase.process(
                optionalRun.get()
        );
    }
}