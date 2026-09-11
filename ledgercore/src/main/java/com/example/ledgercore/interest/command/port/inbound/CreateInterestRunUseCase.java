package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.enums.InterestRunType;

import java.time.LocalDate;

public interface CreateInterestRunUseCase {

    void execute(
            LocalDate businessDate,
            InterestRunType runType
    );
}