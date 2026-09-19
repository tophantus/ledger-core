package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.CreditOfferEvaluationResult;
import com.example.ledgercore.credit.command.service.dto.EvaluateCreditOfferCommand;

import java.util.Optional;

public interface CreditOfferEvaluationService {

    Optional<CreditOfferEvaluationResult> evaluate(
            EvaluateCreditOfferCommand command
    );
}