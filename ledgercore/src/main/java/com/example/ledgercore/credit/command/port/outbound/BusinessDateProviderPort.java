package com.example.ledgercore.credit.command.port.outbound;

import java.time.LocalDate;

public interface BusinessDateProviderPort {

    LocalDate getCurrentBusinessDate();
}