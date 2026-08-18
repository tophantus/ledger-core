package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.TokenResponse;
import com.example.ledgercore.auth.command.dto.VerifyOtpCommand;
import com.example.ledgercore.auth.command.port.inbound.VerifyOtpUseCase;
import com.example.ledgercore.auth.command.port.outbound.OtpVerificationPort;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.auth.service.JwtService;
import com.example.ledgercore.auth.service.RefreshTokenService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyOtpHandler implements VerifyOtpUseCase {

    private final OtpVerificationPort otpVerificationPort;
    private final UserAuthenticationPort userAuthenticationPort;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public TokenResponse execute(VerifyOtpCommand command) {

        OtpVerificationPort.VerificationResult result =
                otpVerificationPort.verifySignupOtp(
                        command.userId(),
                        command.otp()
                );

        switch (result.status()) {
            case INVALID ->
                    throw new BusinessException(
                            ErrorCode.INVALID_OTP
                    );

            case EXPIRED ->
                    throw new BusinessException(
                            ErrorCode.OTP_EXPIRED
                    );

            case ALREADY_VERIFIED ->
                    throw new BusinessException(
                            ErrorCode.OTP_ALREADY_VERIFIED
                    );

            case VERIFIED -> {
                // Continue
            }
        }

        userAuthenticationPort.activateUser(
                command.userId()
        );

        UserAuthenticationPort.UserAuthenticationInfo user =
                userAuthenticationPort
                        .findById(command.userId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        String accessToken =
                jwtService.generateAccessToken(
                        user.userId(),
                        user.roles()
                );

        String refreshToken =
                refreshTokenService
                        .issue(user.userId())
                        .token();

        return new TokenResponse(
                accessToken,
                refreshToken
        );
    }
}