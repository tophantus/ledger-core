package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.RefreshTokenCommand;
import com.example.ledgercore.auth.command.dto.TokenResponse;
import com.example.ledgercore.auth.command.port.inbound.RefreshTokenUseCase;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.auth.service.JwtService;
import com.example.ledgercore.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenHandler
        implements RefreshTokenUseCase {

    private final RefreshTokenService refreshTokenService;
    private final UserAuthenticationPort userAuthenticationPort;
    private final JwtService jwtService;

    @Override
    @Transactional
    public TokenResponse execute(
            RefreshTokenCommand command
    ) {
        RefreshTokenService.IssuedRefreshToken result =
                refreshTokenService.rotate(
                        command.refreshToken()
                );

        UserAuthenticationPort.UserAuthenticationInfo user =
                userAuthenticationPort
                        .findById(result.userId())
                        .orElseThrow();

        String accessToken =
                jwtService.generateAccessToken(
                        user.userId(),
                        user.email(),
                        user.roles()
                );

        return new TokenResponse(
                accessToken,
                result.token()
        );
    }
}