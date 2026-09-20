package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditOfferResult;

public interface CreateCreditOfferService {

    CreateCreditOfferResult create(
            CreateCreditOfferCommand command
    );
}