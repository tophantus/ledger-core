package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.CreditOfferEvaluationResult;
import com.example.ledgercore.credit.command.service.dto.EvaluateCreditOfferCommand;

public interface CreditOfferEvaluationService {

    CreditOfferEvaluationResult evaluate(
            EvaluateCreditOfferCommand command
    );
}