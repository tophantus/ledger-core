package com.example.ledgercore.interest.command.port.inbound;

import java.time.LocalDate;

public interface CreateInterestAccrualRunUseCase {

    void execute(LocalDate businessDate);
}