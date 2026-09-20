package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.UpdateCreditOfferRunProgressUseCase;
import com.example.ledgercore.credit.command.repository.CreditOfferRunCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCreditOfferRunProgressHandler
        implements UpdateCreditOfferRunProgressUseCase {

    private final CreditOfferRunCommandRepository creditOfferRunCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            UUID lastProcessedId,
            Instant heartbeatAt
    ) {
        creditOfferRunCommandRepository
                .findById(runId)
                .ifPresent(run ->
                        run.updateProgress(
                                lastProcessedId,
                                heartbeatAt
                        )
                );
    }
}