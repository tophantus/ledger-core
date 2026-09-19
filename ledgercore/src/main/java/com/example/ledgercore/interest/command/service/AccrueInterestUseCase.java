package com.example.ledgercore.interest.command.service;

import com.example.ledgercore.interest.command.dto.AccrueInterestCommand;

public interface AccrueInterestUseCase {

    void execute(AccrueInterestCommand command);
}