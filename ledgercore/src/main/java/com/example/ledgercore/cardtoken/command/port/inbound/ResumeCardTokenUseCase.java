package com.example.ledgercore.cardtoken.command.port.inbound;

import com.example.ledgercore.cardtoken.command.dto.ResumeCardTokenCommand;

public interface ResumeCardTokenUseCase {

    void execute(ResumeCardTokenCommand command);
}