package com.example.ledgercore.transaction.command.port.outbound;

import java.time.LocalDate;

public interface BusinessDayPort {

    LocalDate getCurrentBusinessDate();
}