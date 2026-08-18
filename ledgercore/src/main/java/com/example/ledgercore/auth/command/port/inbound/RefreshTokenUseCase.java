package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.RefreshTokenCommand;
import com.example.ledgercore.auth.command.dto.TokenResponse;

public interface RefreshTokenUseCase {

    TokenResponse execute(RefreshTokenCommand command);
}