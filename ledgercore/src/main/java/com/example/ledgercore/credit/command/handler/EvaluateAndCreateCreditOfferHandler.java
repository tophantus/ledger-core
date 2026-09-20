package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.credit.command.port.inbound.EvaluateAndCreateCreditOfferUseCase;
import com.example.ledgercore.credit.command.service.CreateCreditOfferService;
import com.example.ledgercore.credit.command.service.CreditOfferEvaluationService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferCommand;
import com.example.ledgercore.credit.command.service.dto.EvaluateCreditOfferCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluateAndCreateCreditOfferHandler
        implements EvaluateAndCreateCreditOfferUseCase {

    private static final long OFFER_VALIDITY_DAYS = 30;

    private final CreditOfferEvaluationService creditOfferEvaluationService;
    private final CreateCreditOfferService createCreditOfferService;

    @Override
    @Transactional
    public void execute(
            UUID runId,
            UUID customerId
    ) {
        creditOfferEvaluationService
                .evaluate(
                        new EvaluateCreditOfferCommand(customerId)
                )
                .ifPresent(evaluation ->
                        createCreditOfferService.create(
                                new CreateCreditOfferCommand(
                                        evaluation.customerId(),
                                        runId,
                                        null,
                                        evaluation.productId(),
                                        evaluation.approvedLimit(),
                                        evaluation.currency(),
                                        Instant.now().plus(
                                                OFFER_VALIDITY_DAYS,
                                                ChronoUnit.DAYS
                                        )
                                )
                        )
                );
    }
}