package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.LoginCommand;
import com.example.ledgercore.auth.command.dto.TokenResponse;

public interface LoginUseCase {

    TokenResponse execute(LoginCommand command);
}