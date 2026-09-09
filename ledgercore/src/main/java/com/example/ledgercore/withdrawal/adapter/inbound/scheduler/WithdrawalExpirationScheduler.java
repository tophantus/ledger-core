package com.example.ledgercore.withdrawal.adapter.inbound.scheduler;

import com.example.ledgercore.withdrawal.command.port.inbound.ExpireWithdrawalIntentsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "withdrawal.expiration.scheduler",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class WithdrawalExpirationScheduler {

    private final ExpireWithdrawalIntentsUseCase
            expireWithdrawalIntentsUseCase;

    @Scheduled(
            fixedDelayString =
                    "${withdrawal.expiration.scheduler.fixed-delay:1000}"
    )
    public void process() {
        expireWithdrawalIntentsUseCase.execute();
    }
}