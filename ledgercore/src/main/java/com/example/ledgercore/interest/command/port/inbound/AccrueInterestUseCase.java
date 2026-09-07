package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.AccrueInterestCommand;

public interface AccrueInterestUseCase {

    void execute(AccrueInterestCommand command);
}