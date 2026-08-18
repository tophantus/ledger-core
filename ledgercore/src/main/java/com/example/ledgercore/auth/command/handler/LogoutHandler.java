package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.LogoutCommand;
import com.example.ledgercore.auth.command.dto.LogoutResponse;
import com.example.ledgercore.auth.command.port.inbound.LogoutUseCase;
import com.example.ledgercore.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutHandler implements LogoutUseCase {

    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LogoutResponse execute(
            LogoutCommand command
    ) {
        refreshTokenService.revoke(
                command.refreshToken()
        );

        return new LogoutResponse(
                true,
                "Logged out successfully"
        );
    }
}