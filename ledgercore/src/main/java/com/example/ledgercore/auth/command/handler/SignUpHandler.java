package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.SignUpCommand;
import com.example.ledgercore.auth.command.dto.SignUpResponse;
import com.example.ledgercore.auth.command.port.inbound.SignUpUseCase;
import com.example.ledgercore.auth.command.port.outbound.OtpVerificationPort;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.auth.service.PasswordService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpHandler implements SignUpUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final PasswordService passwordService;
    private final OtpVerificationPort otpVerificationPort;

    @Override
    @Transactional
    public SignUpResponse execute(SignUpCommand command) {

        String passwordHash =
                passwordService.hash(command.password());

        UserAuthenticationPort.UserAuthenticationInfo userInfo = userAuthenticationPort.createUser(
                new UserAuthenticationPort.CreateUserData(
                        command.username(),
                        command.email(),
                        passwordHash
                )
        ).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        otpVerificationPort.sendSignupOtp(
                userInfo.userId(),
                userInfo.email()

        );

        return new SignUpResponse(
                userInfo.userId(),
                userInfo.username(),
                userInfo.email(),
                userInfo.active(),
                "OTP sent successfully"
        );
    }
}