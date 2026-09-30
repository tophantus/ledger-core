package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.EnqueueDueRepaymentsUseCase;
import com.example.ledgercore.credit.command.port.outbound.BusinessDateProviderPort;
import com.example.ledgercore.credit.command.port.outbound.RepaymentJobPublisherPort;
import com.example.ledgercore.credit.config.CreditRepaymentProperties;
import com.example.ledgercore.credit.messaging.repayment.RepaymentJob;
import com.example.ledgercore.credit.query.dto.GetDueRepaymentCandidatesResult;
import com.example.ledgercore.credit.query.port.inbound.GetDueRepaymentCandidatesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnqueueDueRepaymentsHandler
        implements EnqueueDueRepaymentsUseCase {

    private final BusinessDateProviderPort businessDateProviderPort;

    private final GetDueRepaymentCandidatesUseCase
            getDueRepaymentCandidatesUseCase;

    private final RepaymentJobPublisherPort
            repaymentJobPublisherPort;

    private final CreditRepaymentProperties
            creditRepaymentProperties;

    @Override
    public void execute() {

        LocalDate businessDate =
                businessDateProviderPort.getCurrentBusinessDate();

        Instant now = Instant.now();

        int batchSize =
                creditRepaymentProperties
                        .getScheduler()
                        .getBatchSize();

        List<GetDueRepaymentCandidatesResult> candidates =
                getDueRepaymentCandidatesUseCase.execute(
                        businessDate,
                        now,
                        batchSize
                );

        for (GetDueRepaymentCandidatesResult candidate : candidates) {
            repaymentJobPublisherPort.publish(
                    new RepaymentJob(candidate.statementId())
            );
        }
    }
}