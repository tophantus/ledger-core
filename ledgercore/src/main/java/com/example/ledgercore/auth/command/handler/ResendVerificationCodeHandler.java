package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.ResendVerificationCodeCommand;
import com.example.ledgercore.auth.command.port.inbound.ResendVerificationCodeUseCase;
import com.example.ledgercore.auth.command.port.outbound.EmailVerificationPort;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResendVerificationCodeHandler
        implements ResendVerificationCodeUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final EmailVerificationPort emailVerificationPort;

    @Override
    @Transactional
    public void execute(ResendVerificationCodeCommand command) {

        UserAuthenticationPort.UserAuthenticationInfo user =
                userAuthenticationPort
                        .findByEmail(command.email())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        if (user.active()) {
            throw new BusinessException(
                    ErrorCode.USER_ALREADY_ACTIVE
            );
        }

        emailVerificationPort.sendVerificationCode(
                user.userId(),
                user.email()
        );
    }
}