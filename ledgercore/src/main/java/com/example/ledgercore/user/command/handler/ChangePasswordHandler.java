package com.example.ledgercore.user.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.user.command.port.inbound.ChangePasswordUseCase;
import com.example.ledgercore.user.command.repository.UserCommandRepository;
import com.example.ledgercore.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordHandler
        implements ChangePasswordUseCase {

    private final UserCommandRepository userCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID userId,
            String passwordHash
    ) {
        User user = userCommandRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (!user.isActive()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_ACTIVE
            );
        }

        user.setPasswordHash(passwordHash);
    }
}