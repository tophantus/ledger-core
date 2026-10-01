package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.EnqueueDueRepaymentsUseCase;
import com.example.ledgercore.credit.command.port.outbound.RepaymentJobPublisherPort;
import com.example.ledgercore.credit.config.CreditRepaymentProperties;
import com.example.ledgercore.credit.messaging.repayment.RepaymentJob;
import com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult;
import com.example.ledgercore.credit.query.port.inbound.GetDueRepaymentCandidatesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnqueueDueRepaymentsHandler
        implements EnqueueDueRepaymentsUseCase {

    private final GetDueRepaymentCandidatesUseCase
            getDueRepaymentCandidatesUseCase;

    private final RepaymentJobPublisherPort
            repaymentJobPublisherPort;

    private final CreditRepaymentProperties
            creditRepaymentProperties;

    @Override
    public void execute() {

        Instant now = Instant.now();

        int batchSize =
                creditRepaymentProperties
                        .getScheduler()
                        .getBatchSize();

        List<GetDueRepaymentCandidatesResult> candidates =
                getDueRepaymentCandidatesUseCase.execute(
                        now,
                        batchSize
                );

        log.debug(
                "Found {} due credit repayment candidates",
                candidates.size()
        );

        for (GetDueRepaymentCandidatesResult candidate : candidates) {
            repaymentJobPublisherPort.publish(
                    new RepaymentJob(candidate.statementId())
            );
        }

        if (!candidates.isEmpty()) {
            log.info(
                    "Enqueued {} credit repayment jobs",
                    candidates.size()
            );
        }
    }
}