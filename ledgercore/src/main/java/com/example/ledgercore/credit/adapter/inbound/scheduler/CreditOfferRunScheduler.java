package com.example.ledgercore.credit.adapter.inbound.scheduler;

import com.example.ledgercore.credit.command.dto.ClaimedCreditOfferRun;
import com.example.ledgercore.credit.command.port.inbound.ClaimCreditOfferRunUseCase;
import com.example.ledgercore.credit.command.service.CreditOfferRunProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "credit.offer.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class CreditOfferRunScheduler {

    private final ClaimCreditOfferRunUseCase claimCreditOfferRunUseCase;
    private final CreditOfferRunProcessor creditOfferRunProcessor;

    @Scheduled(
            fixedDelayString =
                    "${credit.offer.scheduler.fixed-delay:1000}"
    )
    public void process() {

        Optional<ClaimedCreditOfferRun> optionalRun =
                claimCreditOfferRunUseCase.execute(
                        Instant.now()
                );

        if (optionalRun.isEmpty()) {
            return;
        }

        creditOfferRunProcessor.process(
                optionalRun.get()
        );
    }
}