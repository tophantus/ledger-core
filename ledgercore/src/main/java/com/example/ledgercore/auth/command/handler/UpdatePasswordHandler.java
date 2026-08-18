package com.example.ledgercore.auth.command.handler;

import com.example.ledgercore.auth.command.dto.UpdatePasswordCommand;
import com.example.ledgercore.auth.command.port.inbound.UpdatePasswordUseCase;
import com.example.ledgercore.auth.command.port.outbound.UserAuthenticationPort;
import com.example.ledgercore.auth.service.PasswordService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdatePasswordHandler implements UpdatePasswordUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final PasswordService passwordService;

    @Override
    @Transactional
    public void execute(
            UUID userId,
            UpdatePasswordCommand command
    ) {

        UserAuthenticationPort.UserAuthenticationInfo user =
                userAuthenticationPort
                        .findById(userId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        if (!user.active()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_ACTIVE
            );
        }

        if (!passwordService.matches(
                command.currentPassword(),
                user.passwordHash()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_CURRENT_PASSWORD
            );
        }

        if (passwordService.matches(
                command.newPassword(),
                user.passwordHash()
        )) {
            throw new BusinessException(
                    ErrorCode.PASSWORD_SAME_AS_CURRENT
            );
        }

        String newPasswordHash =
                passwordService.hash(command.newPassword());

        userAuthenticationPort.updatePassword(
                userId,
                newPasswordHash
        );
    }
}