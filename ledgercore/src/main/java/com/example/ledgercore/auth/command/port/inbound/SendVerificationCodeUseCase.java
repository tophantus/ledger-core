package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.SendVerificationCodeCommand;

public interface SendVerificationCodeUseCase {

    void execute(SendVerificationCodeCommand command);
}