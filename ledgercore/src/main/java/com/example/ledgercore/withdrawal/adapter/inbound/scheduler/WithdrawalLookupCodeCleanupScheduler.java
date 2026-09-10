package com.example.ledgercore.withdrawal.adapter.inbound.scheduler;

import com.example.ledgercore.withdrawal.command.port.inbound.DeleteExpiredWithdrawalLookupCodesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "withdrawal.lookup-code.cleanup.scheduler",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class WithdrawalLookupCodeCleanupScheduler {

    private final DeleteExpiredWithdrawalLookupCodesUseCase
            deleteExpiredWithdrawalLookupCodesUseCase;

    @Scheduled(
            fixedDelayString =
                    "${withdrawal.lookup-code.cleanup.scheduler.fixed-delay:10000}"
    )
    public void process() {
        deleteExpiredWithdrawalLookupCodesUseCase.execute();
    }
}