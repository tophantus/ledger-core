package com.example.ledgercore.interest.adapter.inbound.scheduler;

import com.example.ledgercore.interest.command.dto.ClaimedInterestRun;
import com.example.ledgercore.interest.command.port.inbound.ClaimInterestRunUseCase;
import com.example.ledgercore.interest.command.port.inbound.DispatchInterestRunUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "interest.scheduler",
        name = "enabled",
        havingValue = "true"
)
public class InterestRunScheduler {

    private final ClaimInterestRunUseCase claimInterestRunUseCase;
    private final DispatchInterestRunUseCase dispatchInterestRunUseCase;

    @Scheduled(
            fixedDelayString =
                    "${interest.scheduler.fixed-delay:1000}"
    )
    public void process() {

        Optional<ClaimedInterestRun> optionalRun =
                claimInterestRunUseCase.execute(Instant.now());

        if (optionalRun.isEmpty()) {
            return;
        }

        dispatchInterestRunUseCase.execute(
                optionalRun.get()
        );
    }
}