package com.example.ledgercore.credit.command.handler.run;

import com.example.ledgercore.credit.command.port.inbound.run.UpdateCreditStatementRunProgressUseCase;
import com.example.ledgercore.credit.command.repository.CreditStatementRunCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCreditStatementRunProgressHandler
        implements UpdateCreditStatementRunProgressUseCase {

    private final CreditStatementRunCommandRepository
            creditStatementRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            UUID lastProcessedFacilityId,
            Instant heartbeatAt
    ) {
        creditStatementRunCommandRepository
                .findById(runId)
                .ifPresent(run ->
                        run.updateProgress(
                                lastProcessedFacilityId,
                                heartbeatAt
                        )
                );
    }
}