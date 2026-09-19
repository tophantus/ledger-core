package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.CompleteCreditOfferRunUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferRunCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompleteCreditOfferRunHandler
        implements CompleteCreditOfferRunUseCase {

    private final CreditOfferRunCommandRepository creditOfferRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            Instant completedAt
    ) {
        creditOfferRunCommandRepository
                .findById(runId)
                .ifPresent(run ->
                        run.complete(completedAt)
                );
    }
}