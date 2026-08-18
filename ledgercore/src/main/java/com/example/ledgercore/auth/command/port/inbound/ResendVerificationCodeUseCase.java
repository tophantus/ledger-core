package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.ResendVerificationCodeCommand;

public interface ResendVerificationCodeUseCase {

    void execute(ResendVerificationCodeCommand command);
}