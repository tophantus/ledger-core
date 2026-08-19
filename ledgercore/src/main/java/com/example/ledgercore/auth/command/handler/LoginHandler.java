package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.LoginCommand;
import com.example.ledgercore.auth.command.dto.LoginResponse;
import com.example.ledgercore.auth.command.dto.TokenResponse;
import com.example.ledgercore.auth.command.enums.LoginStatus;
import com.example.ledgercore.auth.command.port.inbound.LoginUseCase;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.auth.service.JwtService;
import com.example.ledgercore.auth.service.PasswordService;
import com.example.ledgercore.auth.service.RefreshTokenService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginHandler implements LoginUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponse execute(LoginCommand command) {

        UserAuthenticationPort.UserAuthenticationInfo user =
                userAuthenticationPort
                        .findByEmail(command.email())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_CREDENTIALS
                                )
                        );

        if (!passwordService.matches(
                command.password(),
                user.passwordHash()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        if (!user.active()) {
            return new LoginResponse(
                    LoginStatus.EMAIL_NOT_VERIFIED,
                    null,
                    user.userId()
            );
        }

        String accessToken =
                jwtService.generateAccessToken(
                        user.userId(),
                        user.roles()
                );

        String refreshToken =
                refreshTokenService
                        .issue(user.userId())
                        .token();

        return new LoginResponse(
                LoginStatus.AUTHENTICATED,
                new TokenResponse(
                        accessToken,
                        refreshToken
                ),
                user.userId()
        );
    }
}