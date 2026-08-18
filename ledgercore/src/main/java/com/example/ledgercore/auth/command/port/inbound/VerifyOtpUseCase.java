package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.TokenResponse;
import com.example.ledgercore.auth.command.dto.VerifyOtpCommand;

public interface VerifyOtpUseCase {

    TokenResponse execute(VerifyOtpCommand command);
}