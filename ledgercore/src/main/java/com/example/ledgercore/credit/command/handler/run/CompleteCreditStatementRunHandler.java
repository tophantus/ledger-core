package com.example.ledgercore.credit.command.handler.run;

import com.example.ledgercore.credit.command.port.inbound.run.CompleteCreditStatementRunUseCase;
import com.example.ledgercore.credit.command.repository.CreditStatementRunCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompleteCreditStatementRunHandler
        implements CompleteCreditStatementRunUseCase {

    private final CreditStatementRunCommandRepository
            creditStatementRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            Instant completedAt
    ) {
        creditStatementRunCommandRepository
                .findById(runId)
                .ifPresent(run ->
                        run.complete(completedAt)
                );
    }
}