package com.example.ledgercore.credit.command.port.inbound;

import java.time.LocalDate;

public interface ProcessCreditStatementBusinessDayClosedUseCase {

    void execute(LocalDate businessDate);
}