package com.example.ledgercore.credit.command.port.inbound;

import java.time.LocalDate;

public interface CreateCreditOfferRunUseCase {

    void execute(LocalDate businessDate);
}