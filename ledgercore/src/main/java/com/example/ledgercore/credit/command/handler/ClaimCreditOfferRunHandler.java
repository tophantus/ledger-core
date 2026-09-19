package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.dto.ClaimedCreditOfferRun;
import com.example.ledgercore.credit.command.port.inbound.ClaimCreditOfferRunUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferRunCommandRepository;
import com.example.ledgercore.credit.entity.CreditOfferRun;
import com.example.ledgercore.credit.enums.CreditOfferRunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClaimCreditOfferRunHandler
        implements ClaimCreditOfferRunUseCase {

    private static final long HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final CreditOfferRunCommandRepository creditOfferRunCommandRepository;

    @Override
    @Transactional
    public Optional<ClaimedCreditOfferRun> execute(
            Instant claimAt
    ) {
        Instant staleBefore =
                claimAt.minus(
                        HEARTBEAT_TIMEOUT_SECONDS,
                        ChronoUnit.SECONDS
                );

        Optional<CreditOfferRun> run =
                creditOfferRunCommandRepository.findClaimableRun(
                                CreditOfferRunStatus.PENDING.name(),
                                CreditOfferRunStatus.RUNNING.name(),
                                staleBefore
                        );

        if (run.isEmpty()) {
            return Optional.empty();
        }

        CreditOfferRun creditOfferRun = run.get();

        creditOfferRun.start(claimAt);

        creditOfferRunCommandRepository.save(creditOfferRun);

        return Optional.of(
                new ClaimedCreditOfferRun(
                        creditOfferRun.getId(),
                        creditOfferRun.getBusinessDate(),
                        creditOfferRun.getLastProcessedId()
                )
        );
    }
}