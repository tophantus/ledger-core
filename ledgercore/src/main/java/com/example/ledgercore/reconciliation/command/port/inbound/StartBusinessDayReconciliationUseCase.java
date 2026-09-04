package com.example.ledgercore.reconciliation.command.port.inbound;

import java.time.LocalDate;

public interface StartBusinessDayReconciliationUseCase {

    void execute(LocalDate businessDate);
}