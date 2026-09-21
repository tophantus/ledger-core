package com.example.ledgercore.card.adapter.inbound.scheduler;

import com.example.ledgercore.card.command.port.inbound.ExpireCardAuthorizationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "card.authorization.expiration.scheduler",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class CardAuthorizationExpirationScheduler {

    private final ExpireCardAuthorizationsUseCase
            expireCardAuthorizationsUseCase;

    @Scheduled(
            fixedDelayString =
                    "${card.authorization.expiration.scheduler.fixed-delay:1000}"
    )
    public void process() {
        expireCardAuthorizationsUseCase.execute();
    }
}