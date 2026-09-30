package com.example.ledgercore.credit.command.handler.run;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditStatementRun;
import com.example.ledgercore.credit.command.port.inbound.run.ClaimCreditStatementRunUseCase;
import com.example.ledgercore.credit.command.repository.CreditStatementRunCommandRepository;
import com.example.ledgercore.credit.entity.CreditStatementRun;
import com.example.ledgercore.credit.enums.CreditStatementRunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClaimCreditStatementRunHandler
        implements ClaimCreditStatementRunUseCase {

    private static final long HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final CreditStatementRunCommandRepository
            creditStatementRunCommandRepository;

    @Override
    @Transactional
    public Optional<ClaimedCreditStatementRun> execute(
            Instant claimAt
    ) {
        Instant staleBefore = claimAt.minus(
                HEARTBEAT_TIMEOUT_SECONDS,
                ChronoUnit.SECONDS
        );

        Optional<CreditStatementRun> run =
                creditStatementRunCommandRepository.findClaimableRun(
                        CreditStatementRunStatus.PENDING.name(),
                        CreditStatementRunStatus.RUNNING.name(),
                        staleBefore
                );

        if (run.isEmpty()) {
            return Optional.empty();
        }

        CreditStatementRun creditStatementRun = run.get();

        creditStatementRun.start(claimAt);

        creditStatementRunCommandRepository.save(
                creditStatementRun
        );

        return Optional.of(
                new ClaimedCreditStatementRun(
                        creditStatementRun.getId(),
                        creditStatementRun.getPeriodStart(),
                        creditStatementRun.getPeriodEnd(),
                        creditStatementRun.getStatementDate(),
                        creditStatementRun.getDueDate(),
                        creditStatementRun.getLastProcessedFacilityId()
                )
        );
    }
}