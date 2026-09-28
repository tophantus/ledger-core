package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.AccrueCreditInterestCommand;

public interface AccrueCreditInterestUseCase {

    void execute(AccrueCreditInterestCommand command);
}
