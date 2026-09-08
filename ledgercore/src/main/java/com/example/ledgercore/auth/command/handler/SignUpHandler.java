package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.SignUpCommand;
import com.example.ledgercore.auth.command.dto.SignUpResponse;
import com.example.ledgercore.auth.command.port.inbound.SignUpUseCase;
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

    @Override
    @Transactional
    public SignUpResponse execute(SignUpCommand command) {

        String passwordHash =
                passwordService.hash(command.password());

        UserAuthenticationPort.UserAuthenticationInfo userInfo = userAuthenticationPort.createUser(
                new UserAuthenticationPort.CreateUserData(
                        command.fullName(),
                        command.email(),
                        passwordHash
                )
        ).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new SignUpResponse(
                userInfo.userId(),
                userInfo.email(),
                userInfo.active()
        );
    }
}