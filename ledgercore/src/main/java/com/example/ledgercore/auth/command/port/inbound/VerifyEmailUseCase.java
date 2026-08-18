package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.TokenResponse;
import com.example.ledgercore.auth.command.dto.VerifyEmailCommand;

public interface VerifyEmailUseCase {

    TokenResponse execute(VerifyEmailCommand command);
}